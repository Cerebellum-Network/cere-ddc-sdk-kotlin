package network.cere.ddc.nft.config

import java.time.Duration

data class NftStorageConfig(
    val retryTimes: Int = 3,
    val retryBackOff: Duration = Duration.ZERO,
)


