package network.cere.ddc.contract.query.command

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.BucketAllocatedEvent
import network.cere.ddc.contract.model.event.BucketCreatedEvent
import network.cere.ddc.contract.model.response.BucketStatus
import network.cere.ddc.contract.model.response.ResultList

interface Buckets {

    suspend fun bucketCreate(value: Balance, bucketParams: String, clusterId: Long): BucketCreatedEvent
    suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long): BucketAllocatedEvent
    suspend fun bucketSettlePayment(bucketId: Long)
    suspend fun bucketGet(bucketId: Long): BucketStatus
    suspend fun bucketList(offset: Long, limit: Long, owner: AccountId? = null): ResultList<BucketStatus>

}