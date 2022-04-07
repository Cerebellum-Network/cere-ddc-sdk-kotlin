package network.cere.ddc.contract.model

data class NodeStatus(
    val nodeId: Long,
    val node: Node,
    val params: String
)
