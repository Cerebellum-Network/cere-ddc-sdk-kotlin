package network.cere.ddc.client

import network.cere.ddc.client.options.ReadOptions
import network.cere.ddc.client.options.StoreOptions
import network.cere.ddc.contract.model.event.BucketCreatedEvent
import network.cere.ddc.contract.model.response.BucketStatus
import network.cere.ddc.contract.model.response.ResultList
import network.cere.ddc.contract.options.BucketParams
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.`ca-storage`.domain.Container
import network.cere.ddc.`ca-storage`.domain.Piece
import network.cere.ddc.`ca-storage`.domain.Query

interface DdcClient {
    suspend fun createBucket(balance: Long, resource: Long, clusterId: Long, bucketParams: BucketParams?): BucketCreatedEvent
    suspend fun accountDeposit(balance: Long)
    suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long)
    suspend fun bucketGet(bucketId: Long): BucketStatus
    suspend fun bucketList(offset: Long, limit: Long, filterOwnerId: String): ResultList<BucketStatus>
    suspend fun store(bucketId: Long, piece: Piece, options: StoreOptions?): DdcUri

    //    suspend fun store(bucketId: Long, file: File, options: StoreOptions?): DdcUri
//    suspend fun read(ddcUri: DdcUri, options: ReadOptions?): File | Piece
    suspend fun read(ddcUri: DdcUri, options: ReadOptions?): Container
    suspend fun search(query: Query): List<Piece>
    suspend fun shareData(bucketId: Long, dekPath: String, publicKeyHex: String): DdcUri
}