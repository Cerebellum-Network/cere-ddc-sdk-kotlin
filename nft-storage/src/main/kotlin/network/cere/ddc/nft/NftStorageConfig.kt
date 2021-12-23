package network.cere.ddc.nft

import network.cere.ddc.core.model.Node
import java.time.Duration

data class NftStorageConfig(
    val trustedNodes: List<Node>,
    val retryTimes: Int = 3,
    val retryBackOff: Duration = Duration.ofMillis(200),
    val requestExpiration: Duration = Duration.ofSeconds(30),
    val requestTimeout: Duration = Duration.ofMinutes(5),
    val connectTimeout: Duration = Duration.ofMinutes(3)
)
