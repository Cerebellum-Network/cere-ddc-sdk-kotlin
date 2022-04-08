package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.event.ClusterCreatedEvent
import network.cere.ddc.contract.model.event.ClusterNodeReplacedEvent
import network.cere.ddc.contract.model.response.ClusterStatus
import network.cere.ddc.contract.model.response.ResultList

interface ClusterCommander {

    suspend fun clusterCreate(value: Balance, manager: AccountId, partitionCount: Long, nodeIds: List<Long>, clusterParams: String): ClusterCreatedEvent
    suspend fun clusterReserveResource(clusterId: Long, amount: Long)
    suspend fun clusterReplaceNode(clusterId: Long, partitionId: Long, newNodeId: Long): ClusterNodeReplacedEvent
    suspend fun clusterGet(clusterId: Long): ClusterStatus
    suspend fun clusterList(offset: Long, limit: Long, filterManagerId: AccountId? = null): ResultList<ClusterStatus>
    suspend fun clusterDistributeRevenues(clusterId: Long)

}