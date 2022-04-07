package network.cere.ddc.contract.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.mapping.BucketCreateResultReader
import network.cere.ddc.contract.mapping.BucketReader
import network.cere.ddc.contract.mapping.BucketStatusReader
import network.cere.ddc.contract.model.Bucket
import network.cere.ddc.contract.model.BucketCreateResult
import network.cere.ddc.contract.model.BucketStatus

fun main() {
    val config =
        ContractConfig(
            "wss://rpc.testnet.cere.network:9945",
            "5D1fsUPyamMquGDZxSKj3E1YmG3QWp6tMHPktP5DPas3pccr",
            "0xa8febb424024a1e2d3af3bdb70e9f56595ab856c6a077a09f969e6b37b7f4266a43ba7dabfe805e8d363859a0d47540beec5cd83af9814831606e332176f5aa9"
        )
    val client = SmartContractClient(config)
    val bucket = BucketSmartContractClient(client)

    runBlocking {
        client.use {
            client.connect()
            bucket.createBucket("some")
        }
    }

}

class BucketSmartContractClient(private val client: SmartContractClient) {

    private companion object {
        const val BUCKET_GET_HASH = "3802cb77"
        const val BUCKET_CREATE_HASH = "0aeb2379"
        const val BUCKET_ALLOC_INTO_CLUSTER_HASH = "4c482d19"
        const val BUCKET_LIST_STATUSES_HASH = "0b04c5f2"
        const val BUCKET_GET_STATUS_HASH = "cef55af6"
    }

    suspend fun getBucket(bucketId: Long): Bucket {
        val response = client.call(BUCKET_GET_HASH) {
            writeUint32(bucketId)
        }

        return response.read(BucketReader)
    }

    suspend fun getBucketStatus(bucketId: Long): BucketStatus? {
        val response = client.call(BUCKET_GET_STATUS_HASH) {
            writeUint32(bucketId)
        }

        return response.read(BucketStatusReader)
    }

    //As Sequence or channel
    suspend fun getBucketListStatuses(scope: CoroutineScope) {
        client.call(BUCKET_LIST_STATUSES_HASH) {

        }
    }

    suspend fun createBucket(bucketParam: String): BucketCreateResult {
        val response = client.callTransaction(BUCKET_CREATE_HASH) {
            writeString(bucketParam)
        }

        return response.read(BucketCreateResultReader)
    }

    suspend fun allocateBucketIntoCluster() {
        /*val response = client.callTransaction(BUCKET_ALLOC_INTO_CLUSTER_HASH) {

        }*/
    }

}