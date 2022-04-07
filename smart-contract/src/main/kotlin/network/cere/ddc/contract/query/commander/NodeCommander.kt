package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.NodeStatus
import network.cere.ddc.contract.model.ResultList

interface NodeCommander {

    //payable
    fun nodeCreate(rentPerMonth: Balance, nodeParams: String, capacity: Long): Long
    //result
    fun nodeGet(nodeId: Long): NodeStatus
    fun nodeList(offset: Long, limit: Long, filterProviderId: AccountId? = null): ResultList<NodeStatus>

}