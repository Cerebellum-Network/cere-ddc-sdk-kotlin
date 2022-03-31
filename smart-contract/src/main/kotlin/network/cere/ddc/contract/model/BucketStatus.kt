package network.cere.ddc.contract.model

data class BucketStatus(
    val bucketId: Long,
    val bucket: Bucket,
    val writerIds: List<AccountId>,
    val dealStatuses: List<DealStatus>
)