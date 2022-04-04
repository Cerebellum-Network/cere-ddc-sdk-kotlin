package network.cere.ddc.contract.config

data class ContractConfig(
    val wsUrl: String,
    val contractAddress: String,
    val privateKeyHex: String,
    val decimals: Int = 15
)
