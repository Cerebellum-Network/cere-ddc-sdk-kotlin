package network.cere.ddc.contract.model

data class BucketStatus(
    val bucketId: Long,
    val bucket: Bucket,
    val writerIds: List<AccountId>,
    val dealStatuses: List<DealStatus>
) {
    data class Bucket(
        val ownerId: AccountId,
        val clusterIds: List<Long>,
        val dealIds: List<Long>,
        val bucketParams: String
    )
}