package network.cere.ddc.contract.model

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
