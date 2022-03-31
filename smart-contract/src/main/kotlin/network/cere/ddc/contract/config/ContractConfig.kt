package network.cere.ddc.contract.config

data class ContractConfig(
    val httpUrl: String,
    val contractAddress: String,
    val privateKeyHex: String,
    val decimals: Int = 15
)
