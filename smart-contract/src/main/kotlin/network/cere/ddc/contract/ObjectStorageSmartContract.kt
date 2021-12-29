package network.cere.ddc.contract

import network.cere.ddc.contract.model.BucketStatus

interface ObjectStorageSmartContract {

    suspend fun getBucketStatus(bucketId: Long, providerId: String): BucketStatus
    suspend fun createBucket(providerId: String): Long
    suspend fun topUpBucket(bucketId: Long)
}