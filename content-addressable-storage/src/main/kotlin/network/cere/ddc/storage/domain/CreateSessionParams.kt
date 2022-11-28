package network.cere.ddc.storage.domain

data class CreateSessionParams(
    val gas: Int,
    val endOfEpoch: Long,
    val bucketId: Long
)
