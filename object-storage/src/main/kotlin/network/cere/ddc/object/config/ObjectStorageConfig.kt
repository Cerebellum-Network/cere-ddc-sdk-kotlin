package network.cere.ddc.`object`.config

import java.time.Duration

data class ObjectStorageConfig(
    val retryTimes: Int = 3,
    val retryBackOff: Duration = Duration.ZERO,
)


