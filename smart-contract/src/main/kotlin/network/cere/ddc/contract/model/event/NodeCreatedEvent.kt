package network.cere.ddc.contract.model.event

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance

data class NodeCreatedEvent(
    val nodeId: Long,
    val providerId: AccountId,
    val rentPerMonth: Balance,
    val nodeParams: String
)