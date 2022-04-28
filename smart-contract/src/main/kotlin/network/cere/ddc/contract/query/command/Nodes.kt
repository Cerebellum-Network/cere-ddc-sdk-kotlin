package network.cere.ddc.contract.query.command

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.NodeCreatedEvent
import network.cere.ddc.contract.model.response.NodeStatus
import network.cere.ddc.contract.model.response.ResultList

interface Nodes {

    suspend fun nodeCreate(value: Balance, rentPerMonth: Balance, nodeParams: String, capacity: Long, predictGasLimit: Boolean = false): NodeCreatedEvent
    suspend fun nodeGet(nodeId: Long): NodeStatus
    suspend fun nodeList(offset: Long, limit: Long, filterProviderId: AccountId? = null): ResultList<NodeStatus>

}