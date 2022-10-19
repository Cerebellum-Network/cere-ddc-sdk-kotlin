package network.cere.ddc.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iwebpp.crypto.TweetNacl.Box
import network.cere.ddc.client.options.ReadOptions
import network.cere.ddc.client.options.StoreOptions
import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.BucketSmartContract
import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.BucketCreatedEvent
import network.cere.ddc.contract.model.response.BucketStatus
import network.cere.ddc.contract.model.response.ResultList
import network.cere.ddc.contract.options.BucketParams
import network.cere.ddc.contract.options.ClientOptions
import network.cere.ddc.core.encryption.EncryptedData
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.core.uri.Protocol
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.ContentAddressableStorage.Companion.DEK_PATH_TAG
import network.cere.ddc.storage.FileStorage
import network.cere.ddc.storage.KeyValueStorage
import network.cere.ddc.storage.domain.Container
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.PieceUri
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.Tag
import org.bouncycastle.jcajce.provider.digest.Blake2b

class DdcClientImpl(
    private val caStorage: ContentAddressableStorage,
    private val smartContract: BucketSmartContract,
    private val options: ClientOptions,
    private val encryptionSecretPhrase: String
) : DdcClient {

    val kvStorage: KeyValueStorage = KeyValueStorage(caStorage)
    val fileStorage: FileStorage = FileStorage(caStorage, options.fileOptions)
    private val masterDek: ByteArray = Blake2b.Blake2b256().digest(encryptionSecretPhrase.toByteArray())//TODO is .toByteArray() ok?
    private val boxKeypair: Box.KeyPair = Box.keyPair_fromSecretKey(encryptionSecretPhrase.toByteArray())//TODO is .toByteArray() ok?

    companion object {
        const val ENCRYPTOR_TAG = "encryptor"
        const val MAX_BUCKET_SIZE = 5
        suspend fun buildAndConnect(options: ClientOptions, secretPhrase: String, encryptionSecretPhrase: String?): DdcClient {
            val encryptionSecretPhrase = encryptionSecretPhrase ?: secretPhrase;

            val scheme = Scheme.create(options.schemeType, secretPhrase)
            val config = BlockchainConfig(options.smartContract.rpcUrl, options.smartContract.contractAddress, encryptionSecretPhrase)
            val bucketContractConfig = BucketContractConfig(options.smartContract.abi.toPath())
            val smartContract = BucketSmartContract.buildAndConnect(config, bucketContractConfig);
            val caStorage = ContentAddressableStorage(scheme, options.cdnUrl);

            return DdcClientImpl(caStorage, smartContract, options, encryptionSecretPhrase);
        }
    }

    override suspend fun createBucket(balance: Long, resource: Long, clusterId: Long, bucketParams: BucketParams?): BucketCreatedEvent {
        var resourceChanged = resource
        if (resourceChanged > MAX_BUCKET_SIZE) {
            throw Exception("Exceed bucket size. Should be less than $MAX_BUCKET_SIZE")
        } else if (resourceChanged <= 0) {
            resourceChanged = 1 //Is it better throw Exception?
        }
        val event = smartContract.bucketCreate(Balance(balance.toBigInteger()), jacksonObjectMapper().writeValueAsString(bucketParams), clusterId)
        if (balance > 0) {
            smartContract.accountDeposit(Balance(balance.toBigInteger()))
        }

        val clusterStatus = smartContract.clusterGet(clusterId)
        val bucketSize = if (clusterStatus.cluster.vnodes.isNotEmpty())
            (resourceChanged * 1000) / clusterStatus.cluster.vnodes.size
        else 0

        smartContract.bucketAllocIntoCluster(event.bucketId, bucketSize)

        return event
    }

    override suspend fun accountDeposit(balance: Long) {
        smartContract.accountDeposit(Balance(balance.toBigInteger()))
    }

    override suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long) {
        val bucketStatus = bucketGet(bucketId)
        val clusterStatus = smartContract.clusterGet(bucketStatus.bucket.clusterId)

        val total = if (clusterStatus.cluster.vnodes.isNotEmpty())
            bucketStatus.bucket.resourceReserved * clusterStatus.cluster.vnodes.size / 1000 + resource
        else 0
        if (total > MAX_BUCKET_SIZE) {
            throw Exception("Exceed bucket size. Should be less than $MAX_BUCKET_SIZE")
        }

        val resourceToAlloc = if (clusterStatus.cluster.vnodes.isNotEmpty())
            (resource * 1000) / clusterStatus.cluster.vnodes.size
        else 0
        smartContract.bucketAllocIntoCluster(bucketId, resourceToAlloc)
    }

    override suspend fun bucketGet(bucketId: Long): BucketStatus {
        return smartContract.bucketGet(bucketId)
    }

    override suspend fun bucketList(offset: Long, limit: Long, filterOwnerId: String): ResultList<BucketStatus> {
        return smartContract.bucketList(offset, limit, AccountId(filterOwnerId))
    }

    override suspend fun store(bucketId: Long, piece: Piece, options: StoreOptions?): DdcUri {
        return if (options != null && options.encrypt) {
            storeEncrypted(bucketId, piece, options)
        } else {
            storeUnencrypted(bucketId, piece)
        }
    }

    private suspend fun storeUnencrypted(bucketId: Long, piece: Piece): DdcUri {
        return caStorage.store(bucketId, piece)
    }

    private suspend fun storeEncrypted(bucketId: Long, fileOrPiece: Container, options: StoreOptions): DdcUri {
        var dek = buildHierarchicalDekHex(masterDek, options.dekPath)
        //ToDo can be random (Nacl ScaleBox). We need to decide if we need store publickey of user who created edek or shared
        val box = Box(boxKeypair.publicKey, boxKeypair.secretKey)
        var edek = box.box(dek)
        //ToDo need better structure to store keys
        val tags = mutableListOf(Tag(ENCRYPTOR_TAG, boxKeypair.publicKey.decodeToString()), Tag("Key", "${bucketId}/${options.dekPath}/${boxKeypair.publicKey.decodeToString()}"))
        val p = Piece(edek, tags)
        caStorage.store(bucketId, p)

        val encryptionOptions = EncryptionOptions(options.dekPath ?: "", dek)

        if (fileOrPiece is Piece) {
            val pieceUri = caStorage.storeEncrypted(bucketId, fileOrPiece, encryptionOptions)
            return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IPIECE).build()
        } else {
            val pieceUri = fileStorage.uploadEncrypted(
                bucketId,
                fileOrPiece.data,
                fileOrPiece.tags,
                encryptionOptions,
            )
            return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IFILE).build()
        }
    }

    private suspend fun downloadDek(bucketId: Long, dekPath: String): ByteArray {
        val pieces = kvStorage.read(bucketId, "${bucketId}/${dekPath}/${boxKeypair.publicKey.decodeToString()}")
        if (pieces.isEmpty()) {
            throw Exception("Client EDEK not found")
        }
        val piece = pieces[0]

        val encryptor = piece.tags.find { it.key === ENCRYPTOR_TAG }?.value ?: throw Exception("EDEK doesn't contains encryptor public key")
        val box = Box(encryptor.encodeToByteArray(), boxKeypair.secretKey)
        val result = box.open(piece.data) ?: throw Exception("Unable to decrypt dek")
        return result
    }

    private fun buildHierarchicalDekHex(dek: ByteArray, dekPath: String?): ByteArray {
        if (dekPath == null) {
            return dek
        }

        val pathParts = dekPath.split("/")
        var increasingDek = dek
        for (part in pathParts) {
            val data = increasingDek + part.encodeToByteArray()
            increasingDek = Blake2b.Blake2b256().digest(data)
        }

        return increasingDek
    }

    private suspend fun findDek(ddcUri: DdcUri, piece: Piece, options: ReadOptions?): ByteArray {
        if (options != null && options.decrypt) {
            val dekPath = piece.tags.find { it.key == DEK_PATH_TAG }?.value
            if (dekPath == null) {
                throw Exception("Piece=${ddcUri} doesn't have dekPath")
            } else if (!dekPath.startsWith(options.dekPath!! + "/") && dekPath !== options.dekPath) {
                throw Exception("Provided dekPath='${options.dekPath}' doesn't correct for piece with dekPath='${dekPath}'")
            }

            val clientDek = downloadDek(ddcUri.bucketId, options.dekPath)

            return buildHierarchicalDekHex(clientDek, dekPath.replace(options.dekPath, "").replace("/", ""))
        }

        return ByteArray(8)
    }

    private suspend fun readByPieceUri(ddcUri: DdcUri, headPiece: Piece, options: ReadOptions?): Piece {
        val isEncrypted = headPiece.tags.any { it.key == DEK_PATH_TAG }
        val nonce = headPiece.tags.first { it.key == ContentAddressableStorage.NONCE_TAG }.value

        //TODO 4. put into DEK cache
        val dek = findDek(ddcUri, headPiece, options)

        if (headPiece.links.isNotEmpty()) {
            TODO("Not yet implemented")
            //read file
        } else {
            if (options != null && isEncrypted && options.decrypt) {
                headPiece.data = caStorage.cipher.decrypt(EncryptedData(headPiece.data, nonce.hexToBytes()), dek)
            }

            return headPiece
        }
    }

    override suspend fun read(ddcUri: DdcUri, options: ReadOptions?): Container {
        if (ddcUri.protocol != null) {
            val pieceUri = PieceUri(ddcUri.bucketId, ddcUri.cid)
            val piece = caStorage.read(pieceUri.bucketId, pieceUri.cid)
            if (options != null && options.decrypt) {
                val dek = findDek(ddcUri, piece, options)
                val nonce = caStorage.cipher.getEmptyNonce()//TODO for now using empty nonce
                piece.data = caStorage.cipher.decrypt(EncryptedData(piece.data, nonce), dek)
            }
            return piece
        }
        val headPiece = caStorage.read(ddcUri.bucketId, ddcUri.cid)
        return readByPieceUri(ddcUri, headPiece, options)
    }

    override suspend fun search(query: Query): List<Piece> {
        val result = caStorage.search(query)
        return result.pieces
    }

    override suspend fun shareData(bucketId: Long, dekPath: String, partnerBoxPublicKey: String): DdcUri {
        val dek = buildHierarchicalDekHex(masterDek, dekPath)
        val box = Box(boxKeypair.publicKey, boxKeypair.secretKey)
        val partnerEdek = box.box(dek)
        val tags = mutableListOf(Tag(ENCRYPTOR_TAG, boxKeypair.publicKey.decodeToString()), Tag("Key", "${bucketId}/${dekPath}/${partnerBoxPublicKey}"))
        val pieceUri = caStorage.store(bucketId, Piece(partnerEdek, tags))
        return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IPIECE).build()
    }

}