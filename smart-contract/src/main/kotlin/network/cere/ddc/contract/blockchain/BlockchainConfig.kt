package network.cere.ddc.contract.blockchain

import java.nio.file.Path

data class BlockchainConfig(
    val wsUrl: String,
    val contractAddressHex: String,
    val privateKeyHex: String,
    val typeFiles: List<Path> = listOf()
)
