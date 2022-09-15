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
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.storage.ContentAddressableStorage
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

    override suspend fun read(pieceUri: PieceUri, options: ReadOptions?): Piece {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: Query): Array<Piece> {
        TODO("Not yet implemented")
    }

    override suspend fun shareData(bucketId: Long, dekPath: String, publicKeyHex: String): PieceUri {
        TODO("Not yet implemented")
    }
}