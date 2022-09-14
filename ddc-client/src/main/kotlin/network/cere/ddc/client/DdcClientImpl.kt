package network.cere.ddc.client

import com.iwebpp.crypto.TweetNacl.Box
import network.cere.ddc.client.options.ReadOptions
import network.cere.ddc.client.options.StoreOptions
import network.cere.ddc.contract.BucketSmartContract
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.BucketCreatedEvent
import network.cere.ddc.contract.model.response.BucketStatus
import network.cere.ddc.contract.options.BucketParams
import network.cere.ddc.contract.options.ClientOptions
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.FileStorage
import network.cere.ddc.storage.KeyValueStorage
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Query
import org.bouncycastle.jcajce.provider.digest.Blake2b
import java.math.BigInteger

class DdcClientImpl(val caStorage: ContentAddressableStorage, val smartContract: BucketSmartContract, val options: ClientOptions, encryptionSecretPhrase: String) : DdcClient {

    val kvStorage: KeyValueStorage
    val fileStorage: FileStorage
    val masterDek: ByteArray
    val boxKeypair: Box.KeyPair

    init {
        this.kvStorage = KeyValueStorage(caStorage)
        this.fileStorage = FileStorage(caStorage, options.fileOptions)
        this.masterDek = Blake2b.Blake2b256().digest(encryptionSecretPhrase.toByteArray()) //is .toByteArray() ok?
        this.boxKeypair = Box.keyPair_fromSecretKey(encryptionSecretPhrase.toByteArray()) //is .toByteArray() ok?
    }

    private companion object {
        const val ENCRYPTOR_TAG = "encryptor"
        const val MAX_BUCKET_SIZE = 5
    }

    override suspend fun createBucket(balance: Long, _resource: Long, clusterId: Long, bucketParams: BucketParams?): BucketCreatedEvent {
        var resource = _resource
        if (resource > MAX_BUCKET_SIZE) {
            throw Error("Exceed bucket size. Should be less than $MAX_BUCKET_SIZE");
        } else if (resource <= 0) {
            resource = 1
        }

        val event = this.smartContract.bucketCreate(Balance(balance.toBigInteger()), bucketParams, clusterId);
        if (balance > 0) {
            this.smartContract.accountDeposit(Balance(balance.toBigInteger()));
        }

        val clusterStatus = this.smartContract.clusterGet(clusterId);
        val bucketSize = if (clusterStatus.cluster.vnodes.isNotEmpty())
            BigInteger.valueOf((resource * 1000) / clusterStatus.cluster.vnodes.size)
        else BigInteger.valueOf(0)

        this.smartContract.bucketAllocIntoCluster(event.bucketId, bucketSize);

        return event;
    }

    override suspend fun accountDeposit(balance: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun bucketGet(bucketId: Long): BucketStatus {
        TODO("Not yet implemented")
    }

    override suspend fun bucketList(offset: Long, limit: Long, filterOwnerId: String?): List<BucketStatus> {
        TODO("Not yet implemented")
    }

    override suspend fun store(bucketId: Long, piece: Piece, options: StoreOptions?): DdcUri {
        TODO("Not yet implemented")
    }

    override suspend fun read(ddcUri: DdcUri, options: ReadOptions?): Piece {
        TODO("Not yet implemented")
    }

    override suspend fun search(query: Query): Array<Piece> {
        TODO("Not yet implemented")
    }

    override suspend fun shareData(bucketId: Long, dekPath: String, publicKeyHex: String): DdcUri {
        TODO("Not yet implemented")
    }
}