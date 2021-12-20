package network.cere.ddc.nft

import network.cere.ddc.core.model.Node
import java.time.Duration

data class Config(
    val trustedNodes: List<Node>,
    val requestExpiration: Duration = Duration.ofSeconds(30),
    val requestTimeout: Duration = Duration.ofMinutes(5),
    val connectTimeout: Duration = Duration.ofMinutes(3)
)
