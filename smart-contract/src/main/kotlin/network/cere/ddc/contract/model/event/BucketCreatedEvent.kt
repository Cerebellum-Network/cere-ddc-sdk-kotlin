package network.cere.ddc.contract.model.event

import network.cere.ddc.contract.model.AccountId

data class BucketCreatedEvent(
    val bucketId: Long,
    val ownerId: AccountId,
)