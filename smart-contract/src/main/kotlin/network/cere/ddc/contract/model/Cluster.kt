package network.cere.ddc.contract.model

data class Cluster(
    val managerId: AccountId,
    val vnodes: List<Long>,
    val resourcePerVnode: Long,
    val resourceUsed: Long,
    val revenues: Balance,
    val totalRent: Balance
)
