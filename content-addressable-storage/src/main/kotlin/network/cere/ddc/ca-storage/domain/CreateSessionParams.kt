package network.cere.ddc.`ca-storage`.domain

data class CreateSessionParams(
    val gas: Int,
    val endOfEpoch: Long,
    val bucketId: Long
)
