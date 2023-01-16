package network.cere.ddc.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iwebpp.crypto.TweetNacl.Box
import network.cere.ddc.`ca-storage`.ContentAddressableStorage
import network.cere.ddc.`ca-storage`.ContentAddressableStorage.Companion.DEK_PATH_TAG
import network.cere.ddc.`ca-storage`.ContentAddressableStorage.Companion.NONCE_TAG
import network.cere.ddc.`ca-storage`.domain.File
import network.cere.ddc.`ca-storage`.domain.Piece
import network.cere.ddc.`ca-storage`.domain.PieceUri
import network.cere.ddc.`ca-storage`.domain.Query
import network.cere.ddc.`ca-storage`.domain.Tag
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
import network.cere.ddc.`file-storage`.FileStorage
import network.cere.ddc.`key-value-storage`.KeyValueStorage
import org.bouncycastle.jcajce.provider.digest.Blake2b
import org.komputing.khex.extensions.toHexString

class DdcClientImpl(
    private val caStorage: ContentAddressableStorage,
    private val smartContract: BucketSmartContract,
    private val options: ClientOptions,
    private val encryptionSecretPhrase: String
) : DdcClient {

    val kvStorage: KeyValueStorage = KeyValueStorage(caStorage)
    val fileStorage: FileStorage = FileStorage(caStorage, options.fileOptions)
    private val masterDek: ByteArray = Blake2b.Blake2b256().digest(encryptionSecretPhrase.toByteArray())
    private val boxKeypair: Box.KeyPair = Box.keyPair_fromSecretKey(encryptionSecretPhrase.toByteArray())

    companion object {
        const val ENCRYPTOR_TAG = "encryptor"
        const val MAX_BUCKET_SIZE = 5
        fun buildAndConnect(options: ClientOptions, privateKey: String, encryptionSecretPhrase: String?): DdcClient {
            val encryptionSecretPhrase = encryptionSecretPhrase ?: privateKey

            val scheme = Scheme.create(options.schemeType, privateKey)
            val config = BlockchainConfig(options.smartContract.rpcUrl, options.smartContract.contractAddress, encryptionSecretPhrase)
            val bucketContractConfig = BucketContractConfig(options.smartContract.abi.toPath())
            //TODO unmock when smartcontract will be ready
            //val smartContract = BucketSmartContract.buildAndConnect(config, bucketContractConfig)
            val smartContract = BucketSmartContract.mock(config, bucketContractConfig)
            val caStorage = ContentAddressableStorage(scheme, options.cdnUrl)

            return DdcClientImpl(caStorage, smartContract, options, encryptionSecretPhrase)
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

    override suspend fun upload(bucketId: Long, file: File, options: StoreOptions?): DdcUri {
        val filePath = file.filePath
        return if (options != null && options.encrypt) {
            uploadEncrypted(bucketId, file, options)
        } else if (filePath != null) {
            fileStorage.upload(bucketId, filePath)
        } else {
            throw Exception("There is no file data")
        }
    }

    private suspend fun storeUnencrypted(bucketId: Long, piece: Piece): DdcUri {
        return caStorage.store(bucketId, piece)
    }

    private suspend fun uploadEncrypted(bucketId: Long, file: File, options: StoreOptions): DdcUri {
        val encryptionOptions = constructEncryptionOptions(options, bucketId)
        val filePath = file.filePath
        val pieceUri = if (filePath != null) {
            fileStorage.uploadEncrypted(bucketId, filePath, encryptionOptions)
        } else {
            throw Exception("There is no file data")
        }
        return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IPIECE).build()
    }

    private suspend fun constructEncryptionOptions(options: StoreOptions, bucketId: Long): EncryptionOptions {
        val dek = buildHierarchicalDekHex(masterDek, options.dekPath)
        val box = Box(boxKeypair.publicKey, boxKeypair.secretKey)
        val edek = box.box(dek)
        val tags = mutableListOf(Tag(ENCRYPTOR_TAG, boxKeypair.publicKey.toHexString()), Tag("Key", "${bucketId}/${options.dekPath}/${boxKeypair.publicKey.toHexString()}"))
        val p = Piece(edek, tags)
        caStorage.store(bucketId, p)
        return EncryptionOptions(options.dekPath ?: "", dek)
    }

    private suspend fun storeEncrypted(bucketId: Long, piece: Piece, options: StoreOptions): DdcUri {
        val encryptionOptions = constructEncryptionOptions(options, bucketId)
        val pieceUri = caStorage.storeEncrypted(bucketId, piece, encryptionOptions)
        return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IPIECE).build()
    }

    private suspend fun downloadDek(bucketId: Long, dekPath: String): ByteArray {
        val pieces = kvStorage.read(bucketId, "${bucketId}/${dekPath}/${boxKeypair.publicKey.toHexString()}")
        if (pieces.isEmpty()) {
            throw Exception("Client EDEK not found")
        }
        val piece = pieces[0]

        val encryptor = piece.tags.find { it.key == ENCRYPTOR_TAG }?.value ?: throw Exception("EDEK doesn't contains encryptor public key")
        val box = Box(encryptor.hexToBytes(), boxKeypair.secretKey)
        return box.open(piece.data) ?: throw Exception("Unable to decrypt dek")
    }

    private fun buildHierarchicalDekHex(dek: ByteArray, dekPath: String?): ByteArray {
        if (dekPath.isNullOrEmpty()) {
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

    private suspend fun findDek(ddcUri: DdcUri, dekPath: String?, options: ReadOptions?): ByteArray {
        if (options != null && options.decrypt) {
            if (dekPath == null) {
                throw Exception("Piece=${ddcUri} doesn't have dekPath")
            } else if (!dekPath.startsWith(options.dekPath!!) && dekPath !== options.dekPath) {
                throw Exception("Provided dekPath='${options.dekPath}' doesn't correct for piece with dekPath='${dekPath}'")
            }

            val clientDek = downloadDek(ddcUri.bucketId, options.dekPath)

            return buildHierarchicalDekHex(clientDek, dekPath.replace(options.dekPath, "").replace("/", ""))
        }

        return ByteArray(8)
    }

    private suspend fun readByPieceUri(ddcUri: DdcUri, headPiece: Piece, options: ReadOptions?): Piece {
        val isEncrypted = headPiece.tags.any { it.key == DEK_PATH_TAG }
        val nonce = headPiece.tags.first { it.key == NONCE_TAG }.value

        val dekPath = headPiece.tags.find { it.key == DEK_PATH_TAG }?.value
        val dek = findDek(ddcUri, dekPath, options)

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

    override suspend fun read(ddcUri: DdcUri, options: ReadOptions?): Piece {
        if (ddcUri.protocol != null) {
            val pieceUri = PieceUri(ddcUri.bucketId, ddcUri.cid)
            val piece = caStorage.read(pieceUri.bucketId, pieceUri.cid)
            if (options != null && options.decrypt) {
                val dekPath = piece.tags.find { it.key == DEK_PATH_TAG }?.value
                val dek = findDek(ddcUri, dekPath, options)
                val nonce = piece.tags.find { it.key == NONCE_TAG }!!.value.encodeToByteArray()
                piece.data = caStorage.cipher.decrypt(EncryptedData(piece.data, nonce), dek)
            }
            return piece
        }
        val headPiece = caStorage.read(ddcUri.bucketId, ddcUri.cid)
        return readByPieceUri(ddcUri, headPiece, options)
    }

    override suspend fun readFile(ddcUri: DdcUri, options: ReadOptions?): File {
        if (ddcUri.protocol != null) {
            var data = fileStorage.read(ddcUri.bucketId, ddcUri.cid)
            val nonce = ddcUri.nonce
            if (options != null && options.decrypt && nonce != null) {
                val dek = findDek(ddcUri, options.dekPath, options)
                data = caStorage.cipher.decrypt(EncryptedData(data, nonce), dek)
            }
            return File(data)
        }
        return File(fileStorage.read(ddcUri.bucketId, ddcUri.cid))
    }

    override suspend fun search(query: Query): List<Piece> {
        val result = caStorage.search(query)
        return result.pieces
    }

    override suspend fun shareData(bucketId: Long, dekPath: String, publicKeyHex: String): DdcUri {
        val dek = buildHierarchicalDekHex(masterDek, dekPath)
        val box = Box(boxKeypair.publicKey, boxKeypair.secretKey)
        val partnerEdek = box.box(dek)
        val tags = mutableListOf(Tag(ENCRYPTOR_TAG, boxKeypair.publicKey.toHexString()), Tag("Key", "${bucketId}/${dekPath}/${publicKeyHex}"))
        val pieceUri = caStorage.store(bucketId, Piece(partnerEdek, tags))
        return DdcUri.Builder().bucketId(pieceUri.bucketId).cid(pieceUri.cid).protocol(Protocol.IPIECE).build()
    }

}