package network.cere.ddc.contract.model.response

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
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
        val flow: Flow,
        val resourceReserved: Long
    )

    data class Flow(
        val from: AccountId,
        val schedule: Schedule,
    )

    data class Schedule(
        val rate: Balance,
        val offset: Balance
    )
}