package network.cere.ddc.contract.model.response

import network.cere.ddc.contract.model.AccountId
import java.time.LocalDateTime

data class BucketStatus(
    val bucketId: Long,
    val bucket: Bucket,
    val params: String,
    val writerIds: List<AccountId>,
    val rentCoveredUntil: LocalDateTime
) {
    data class Bucket(
        val ownerId: AccountId,
        val clusterId: Long,
        val resourceReserved: Long
    )
}