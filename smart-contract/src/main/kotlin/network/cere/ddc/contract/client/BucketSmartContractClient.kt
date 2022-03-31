package network.cere.ddc.contract.client

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.mapping.BucketReader
import network.cere.ddc.contract.mapping.BucketStatusReader
import network.cere.ddc.contract.model.Bucket
import network.cere.ddc.contract.model.BucketStatus
import network.cere.ddc.core.extension.hexToBytes

fun main() {
    val config =
        ContractConfig(
            "https://rpc.testnet.cere.network:9934",
            "5D1fsUPyamMquGDZxSKj3E1YmG3QWp6tMHPktP5DPas3pccr",
            "0xa8febb424024a1e2d3af3bdb70e9f56595ab856c6a077a09f969e6b37b7f4266a43ba7dabfe805e8d363859a0d47540beec5cd83af9814831606e332176f5aa9"
        )
    //"bucket_get".toByteArray().toNoPrefixHexString().also { println(it) }
    val client = BucketSmartContractClient(config)
    //"0x790284ffeec761eed8f18bd6bcc2013e40471037c7749677f01843817166613d02a49d1801b87990c1792e336f1e195a97ca111d3a83d7fa86ee4c662af1c28e6973c9d37f0180bf9c6f257a04af5025fcc1c4c902a023ffdafc60d0e731f1cdfdd1765c86250318001202ff 29ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a392 ?0007404513d40130? 0aeb2379 1c 74657374696e67"
    val ss = "0x790284ffeec761eed8f18bd6bcc2013e40471037c7749677f01843817166613d02a49d1801b87990c1792e336f1e195a97ca111d3a83d7fa86ee4c662af1c28e6973c9d37f0180bf9c6f257a04af5025fcc1c4c902a023ffdafc60d0e731f1cdfdd1765c86250318001202ff29ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a3920007404513d401300aeb23791c74657374696e67"

    //aeb23791c74657374696e67
    String("1c".hexToBytes()).also { print(it) }
    runBlocking {
        kotlin.runCatching { client.some() }.onFailure { it.printStackTrace() }.onSuccess { println("SUCCESS") }
        println("DONE")
        client.close()
    }
}

class BucketSmartContractClient(
    contractConfig: ContractConfig
) : AbstractSmartContractClient(contractConfig) {

    private companion object {
        const val BUCKET_GET_HASH = "3802cb77"
        const val BUCKET_CREATE_HASH = "0aeb2379"
        const val BUCKET_ALLOC_INTO_CLUSTER_HASH = "4c482d19"
        const val BUCKET_LIST_STATUSES_HASH = "0b04c5f2"
        const val BUCKET_GET_STATUS_HASH = "cef55af6"
    }

    suspend fun getBucket(bucketId: Long): Bucket {
        val response = call(BUCKET_GET_HASH) {
            writeUint32(bucketId)
        }.await()

        return response.read(BucketReader)
    }

    suspend fun getBucketStatus(bucketId: Long): BucketStatus? {
        val response = call(BUCKET_GET_STATUS_HASH) {
            writeUint32(bucketId)
        }.await()

        return response.read(BucketStatusReader)
    }

    //As Sequence or channel
    suspend fun getBucketListStatuses() {
        call(BUCKET_LIST_STATUSES_HASH) {

        }
    }

    //Mutable operation
    suspend fun createBucket(bucketParam: String): Long {
        val response = call(BUCKET_CREATE_HASH) {
            writeByteArray(bucketParam.toByteArray())
        }.await()

        return response.readUint32()
    }

    suspend fun allocateBucketIntoCluster() {
        val response = call(BUCKET_ALLOC_INTO_CLUSTER_HASH) {

        }
    }

}