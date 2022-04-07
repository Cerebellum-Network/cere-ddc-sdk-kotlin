package network.cere.ddc.contract.config

import java.io.File
import java.nio.file.Path

data class ContractConfig(
    val wsUrl: String,
    val contractAddress: String,
    val privateKeyHex: String,
    val decimals: Int = 15,
    val typeFiles: List<Path> = listOf(
        Path.of(object {}.javaClass.getResource("${File.separatorChar}default_types.json")!!.path),
        Path.of(object {}.javaClass.getResource("${File.separatorChar}cere_custom_types.json")!!.path)
    )
)
