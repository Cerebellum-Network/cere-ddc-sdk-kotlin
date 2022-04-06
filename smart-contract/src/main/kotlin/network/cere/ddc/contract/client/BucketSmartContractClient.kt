package network.cere.ddc.contract.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.mapping.BucketReader
import network.cere.ddc.contract.mapping.BucketStatusReader
import network.cere.ddc.contract.model.Bucket
import network.cere.ddc.contract.model.BucketStatus
import network.cere.ddc.core.extension.hexToBytes

fun main() {
    //0x1400000000000000482d7c09000000000200000001000000000000000000000000000200000002000000120529ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a39294003b000000a687d54dc41c52637ba12babb48ee0cf43f2a3219f23bb208d2127089071ca670c004464634275636b65743a3a4275636b6574437265617465640000000000000005aba098bdf3c7f269a915fb23212a62dffc16e8c68e27ed2c1d2aca92f31c76b34e87181f8007ff704089fc7c438a30d3c8aca725ef9599fb5f632c9ceeeb8a00020000001106ce33fa080000000000000000000000000000020000000000a00920da01000000000000
    //{"block":{"extrinsics":["0x280403000b706bb4fd7f01","0x1c040f007686c200","0xad0284ffa687d54dc41c52637ba12babb48ee0cf43f2a3219f23bb208d2127089071ca67011c25c4370c5a4549edb7a5e7b78b652ad840b08436fb50e225f13c86c40a3f00e0a6ad5b4718269e75a2570ae4eec84f588d90c47aae66bf1d850fe7413f5588000901001202ff29ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a392000b0000f2052a01600aeb23794c74657374696e674b6f746c696e467265736834"],"header":{"digest":{"logs":["0x0642414245340200000000c134621000000000","0x05424142450101125e4eb9bff56f883e0a55dafe8132ef6702352eda8e0441bf7329c18f26f351da840b30fdf727dea6db49d25c36a4663bd9116b5b26e6ad2260788270bd248c"]},"extrinsicsRoot":"0x609d7679a3d7d45badb46c37c8adea4c14aae605d65770c282c7e6eb8043778f","number":"0x30a1a0","parentHash":"0xa6303f4a039f347c2d92fae4e7cfa5a2a25ee8194e2237735bde869c4d9ab7ce","stateRoot":"0xb0de402fa0e3d3bf5c8d88e97ef87ca020948fefa29cff64a1c7fd5c4df06e50"}},"justification":null}
    val result =
        "0x1400000000000000482d7c09000000000200000001000000000000000000000000000200000002000000120529ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a39294003b000000a687d54dc41c52637ba12babb48ee0cf43f2a3219f23bb208d2127089071ca670c004464634275636b65743a3a4275636b6574437265617465640000000000000005aba098bdf3c7f269a915fb23212a62dffc16e8c68e27ed2c1d2aca92f31c76b34e87181f8007ff704089fc7c438a30d3c8aca725ef9599fb5f632c9ceeeb8a00020000001106ce33fa080000000000000000000000000000020000000000a00920da01000000000000".hexToBytes()
    "0x2".hexToBytes().joinToString().also { println(it) }


    //call()
}

fun call() {
    val config =
        ContractConfig(
            "wss://rpc.testnet.cere.network:9945",
            "5D1fsUPyamMquGDZxSKj3E1YmG3QWp6tMHPktP5DPas3pccr",
            "0xa8febb424024a1e2d3af3bdb70e9f56595ab856c6a077a09f969e6b37b7f4266a43ba7dabfe805e8d363859a0d47540beec5cd83af9814831606e332176f5aa9"
        )
    val client = SmartContractClient(config)
    runBlocking {
        kotlin.runCatching {
            client.connect()

            /* measureTimeMillis {
                 client.callTransaction("0aeb2379") {
                     writeAsList("testingKotlinFresh4".toByteArray())
                 }
             }*/

            //client.something()
        }
    }.onFailure { it.printStackTrace() }
        .onSuccess { println(it) }
    println("DONE")
    client.close()
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

    suspend fun createBucket(bucketParam: String): Long {
        val response = client.callTransaction(BUCKET_CREATE_HASH) {
            writeByteArray(bucketParam.toByteArray())
        }

        return -1L
    }

    suspend fun allocateBucketIntoCluster() {
        val response = client.callTransaction(BUCKET_ALLOC_INTO_CLUSTER_HASH) {

        }
    }

}