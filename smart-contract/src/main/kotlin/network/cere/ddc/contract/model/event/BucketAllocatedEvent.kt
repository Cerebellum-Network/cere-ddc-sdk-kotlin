package network.cere.ddc.contract.model.event

data class BucketAllocatedEvent(
    val bucketId: Long,
    val clusterId: Long,
)
