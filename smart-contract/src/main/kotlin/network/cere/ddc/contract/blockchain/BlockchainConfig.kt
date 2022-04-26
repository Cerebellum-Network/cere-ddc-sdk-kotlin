package network.cere.ddc.contract.blockchain

import java.nio.file.Path
import java.time.Duration

data class BlockchainConfig(
    val wsUrl: String,
    val contractAddressHex: String,
    val privateKeyHex: String,
    val timeout: Duration = Duration.ofMinutes(1),
    val typeFiles: List<Path> = listOf()
)
