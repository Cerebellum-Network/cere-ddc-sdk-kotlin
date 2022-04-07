package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.BucketStatus
import network.cere.ddc.contract.model.ResultList

interface BucketCommander {

    suspend fun bucketCreate(bucketParams: String, clusterId: Long): Long
    suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long)
    suspend fun bucketSettlePayment(bucketId: Long)
    suspend fun bucketGet(bucketId: Long): BucketStatus
    suspend fun bucketList(offset: Long, limit: Long, owner: AccountId? = null): ResultList<BucketStatus>

}