package network.cere.ddc.contract.model.event

data class ClusterNodeReplacedEvent(
    val clusterId: Long,
    val nodeId: Long,
    val positionIndex: Long,

)