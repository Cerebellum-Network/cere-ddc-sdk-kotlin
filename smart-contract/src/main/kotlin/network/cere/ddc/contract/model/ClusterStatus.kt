package network.cere.ddc.contract.model

data class ClusterStatus(
    val clusterId: Long,
    val cluster: Cluster,
    val params: String
)
