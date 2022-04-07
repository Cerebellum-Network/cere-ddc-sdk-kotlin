package network.cere.ddc.contract.query

import network.cere.ddc.contract.blockchain.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.BucketStatusReader
import network.cere.ddc.contract.mapping.ResultListReader
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.BucketStatus
import network.cere.ddc.contract.model.ResultList
import network.cere.ddc.contract.query.commander.BucketCommander

class BucketService(private val client: SmartContractClient) : BucketCommander {

    //TODo make dynamic reading hash by function name from JSON file
    private companion object {
        const val BUCKET_GET_HASH = ""
        const val BUCKET_CREATE_HASH = ""
        const val BUCKET_LIST_STATUSES_HASH = ""
        const val BUCKET_ALLOC_INTO_CLUSTER_HASH = ""
        const val BUCKET_SETTLE_PAYMENT_HASH = ""
    }

    private val bucketStatusListReader = ResultListReader(BucketStatusReader)

    override suspend fun bucketGet(bucketId: Long): BucketStatus {
        val response = client.call(BUCKET_GET_HASH) {
            writeUint32(bucketId)
        }

        return response.checkError().read(BucketStatusReader)
    }

    override suspend fun bucketList(offset: Long, limit: Long, owner: AccountId?): ResultList<BucketStatus> {
        val response = client.call(BUCKET_LIST_STATUSES_HASH) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, owner)
        }

        return response.read(bucketStatusListReader)
    }

    override suspend fun bucketCreate(bucketParams: String, clusterId: Long): Long {
        val response = client.callTransaction(BUCKET_CREATE_HASH) {
            writeString(bucketParams)
            writeUint32(clusterId)
        }

        return response.readUint32()
    }

    override suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun bucketSettlePayment(bucketId: Long) {
        TODO("Not yet implemented")
    }
}