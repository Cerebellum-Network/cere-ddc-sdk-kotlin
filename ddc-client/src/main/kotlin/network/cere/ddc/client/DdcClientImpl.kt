package network.cere.ddc.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.iwebpp.crypto.TweetNacl.Box
import network.cere.ddc.client.options.ReadOptions
import network.cere.ddc.client.options.StoreOptions
import network.cere.ddc.contract.BucketSmartContract
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
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.ContentAddressableStorage.Companion.DEK_PATH_TAG
import network.cere.ddc.storage.FileStorage
import network.cere.ddc.storage.KeyValueStorage
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.PieceUri
import network.cere.ddc.storage.domain.Query
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

    private companion object {
        const val ENCRYPTOR_TAG = "encryptor"
        const val MAX_BUCKET_SIZE = 5
    }

    override suspend fun createBucket(balance: Long, resource: Long, clusterId: Long, bucketParams: BucketParams?): BucketCreatedEvent {
        var resourceChanged = resource
        if (resourceChanged > MAX_BUCKET_SIZE) {
            throw Exception("Exceed bucket size. Should be less than $MAX_BUCKET_SIZE")
        } else if (resourceChanged <= 0) {
            resourceChanged = 1 //Is it better throw Exception?
        }
        val event = this.smartContract.bucketCreate(Balance(balance.toBigInteger()), jacksonObjectMapper().writeValueAsString(bucketParams), clusterId)
        if (balance > 0) {
            this.smartContract.accountDeposit(Balance(balance.toBigInteger()))
        }

        val clusterStatus = this.smartContract.clusterGet(clusterId)
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

    override suspend fun store(bucketId: Long, piece: Piece, options: StoreOptions?): PieceUri {
        return if (options != null && options.encrypt) {
            storeEncrypted(bucketId, piece, options)
        } else {
            storeUnencrypted(bucketId, piece)
        }
    }

    private suspend fun storeUnencrypted(bucketId: Long, piece: Piece): PieceUri {
        return caStorage.store(bucketId, piece)
    }

    private suspend fun storeEncrypted(bucketId: Long, piece: Piece, options: StoreOptions): PieceUri {
//        var dek = DdcClient.buildHierarchicalDekHex(this.masterDek, options.dekPath);
//        //ToDo can be random (Nacl ScaleBox). We need to decide if we need store publickey of user who created edek or shared
//        var edek = nacl.box(dek, emptyNonce, this.boxKeypair.publicKey, this.boxKeypair.secretKey);
//
//        //ToDo need better structure to store keys
//        caStorage.store(bucketId, Piece(edek, [Tag(ENCRYPTOR_TAG, u8aToHex(this.boxKeypair.publicKey)), Tag("Key", `${bucketId}/${options.dekPath || ""}/${u8aToHex(this.boxKeypair.publicKey)}`)]))
//
//        val encryptionOptions = EncryptionOptions(options.dekPath ?: "", dek);
//        return caStorage.storeEncrypted(bucketId, piece, encryptionOptions);
        TODO("Not yet implemented")
    }

    private suspend fun downloadDek(bucketId: Long, dekPath: String): ByteArray {
        val pieces = kvStorage.read(bucketId, "${bucketId}/${dekPath}/${boxKeypair.publicKey.decodeToString()}")
        if (pieces.isEmpty()){
            throw Exception("Client EDEK not found")
        }
        val piece = pieces[0]

        val encryptor = piece.tags.find{ it.key === ENCRYPTOR_TAG}?.value ?: throw Exception("EDEK doesn't contains encryptor public key");
        val box = Box(encryptor.encodeToByteArray(), boxKeypair.secretKey)
        val result = box.open(piece.data) ?: throw Exception("Unable to decrypt dek");
        return result;
    }

    private fun buildHierarchicalDekHex(dek: ByteArray, dekPath: String?): ByteArray {
        if (dekPath == null) {
            return dek
        }

        val pathParts = dekPath.split("/")
        var increasingDek = dek
        for (part in pathParts) {
            val data = increasingDek + part.encodeToByteArray();
            increasingDek = Blake2b.Blake2b256().digest(data)
        }

        return increasingDek
    }

    private suspend fun findDek(pieceUri: PieceUri, piece: Piece, options: ReadOptions?): ByteArray {
        if (options != null && options.decrypt) {
            val dekPath = piece.tags.find{ it.key == DEK_PATH_TAG }?.value;
            if (dekPath == null) {
                throw Exception("Piece=${pieceUri} doesn't have dekPath");
            } else if (!dekPath.startsWith(options.dekPath!! + "/") && dekPath !== options.dekPath!!) {
                throw Exception("Provided dekPath='${options.dekPath}' doesn't correct for piece with dekPath='${dekPath}'");
            }

            val clientDek = downloadDek(pieceUri.bucketId, options.dekPath!!);

            return buildHierarchicalDekHex(clientDek, dekPath.replace(options.dekPath!!, "").replace("/", ""))
        }

        return ByteArray(8);
    }

    private suspend fun readByPieceUri(pieceUri: PieceUri, headPiece: Piece, options: ReadOptions?): Piece {
        val isEncrypted = headPiece.tags.any { it.key == DEK_PATH_TAG };
        val nonce = headPiece.tags.first { it.key == ContentAddressableStorage.NONCE_TAG }.value;

        //TODO 4. put into DEK cache
        val dek = findDek(pieceUri, headPiece, options);

        if (headPiece.links.isNotEmpty()) {
            TODO("Not yet implemented")
            //read file
        } else {
                if (options != null && isEncrypted && options.decrypt) {
                    headPiece.data = caStorage.cipher.decrypt(EncryptedData(headPiece.data, nonce.hexToBytes()) , dek)
                }

            return headPiece
        }
    }

    override suspend fun read(pieceUri: PieceUri, options: ReadOptions?): Piece {
        if (pieceUri.protocol) {
        val pieceUri = PieceUri(pieceUri.bucket, pieceUri.path as string);
        val piece = caStorage.read(pieceUri.bucketId, pieceUri.cid);
        if (options.decrypt) {
            val dek = this.findDek(pieceUri, piece, options);
            piece.data = caStorage.cipher.decrypt(piece.data, dek);
        }
        return piece;
        }
        val headPiece = caStorage.read(pieceUri.bucket as bigint, pieceUri.path as string);
        return readByPieceUri(pieceUri, headPiece, options);
    }

    override suspend fun search(query: Query): Array<Piece> {
        TODO("Not yet implemented")
    }

    override suspend fun shareData(bucketId: Long, dekPath: String, publicKeyHex: String): PieceUri {
        TODO("Not yet implemented")
    }
}