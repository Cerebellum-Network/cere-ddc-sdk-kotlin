package network.cere.ddc.contract.model.response

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

data class ClusterStatus(
    val clusterId: Long,
    val cluster: Cluster,
    val params: String
) {
    data class Cluster(
        val managerId: AccountId,
        val vnodes: List<Long>,
        val resourcePerVnode: Long,
        val resourceUsed: Long,
        val revenues: Balance,
        val totalRent: Balance
    )
}
