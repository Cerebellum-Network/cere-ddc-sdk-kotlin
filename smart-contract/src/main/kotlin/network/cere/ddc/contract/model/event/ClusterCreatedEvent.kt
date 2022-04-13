package network.cere.ddc.contract.model.event

import network.cere.ddc.contract.model.AccountId

data class ClusterCreatedEvent(
    val clusterId: Long,
    val manager: AccountId,
    val clusterParams: String
)