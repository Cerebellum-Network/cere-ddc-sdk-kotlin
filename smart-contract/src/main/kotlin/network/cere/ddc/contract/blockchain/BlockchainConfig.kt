package network.cere.ddc.contract.blockchain

import java.io.File
import java.nio.file.Path

data class BlockchainConfig(
    val wsUrl: String,
    val contractAddressHex: String,
    val privateKeyHex: String,
    val typeFiles: List<Path> = listOf(
        Path.of(object {}.javaClass.getResource("${File.separatorChar}default_types.json")!!.path),
        Path.of(object {}.javaClass.getResource("${File.separatorChar}cere_custom_types.json")!!.path)
    )
)
