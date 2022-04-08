package network.cere.ddc.contract.model.response

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

data class NodeStatus(
    val nodeId: Long,
    val node: Node,
    val params: String
) {
    data class Node(
        val providerId: AccountId,
        val rentPerMonth: Balance,
        val freeResource: Long
    )
}
