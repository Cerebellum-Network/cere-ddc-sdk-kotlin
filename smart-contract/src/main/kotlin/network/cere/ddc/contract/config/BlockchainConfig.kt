package network.cere.ddc.contract.config

data class BlockchainConfig(
    val httpUrl: String,
    val operationalWallet: String,
    val decimals: Int = 15
)
