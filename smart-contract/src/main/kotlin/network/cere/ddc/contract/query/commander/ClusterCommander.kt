package network.cere.ddc.contract.query.commander

import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.ClusterStatus
import network.cere.ddc.contract.model.ResultList

interface ClusterCommander {

    suspend fun clusterCreate(manager: AccountId, partitionCount: Long, nodeIds: List<Long>, clusterParams: String): Long
    suspend fun clusterReserveResource(clusterId: Long, amount: Long)
    suspend fun clusterReplaceNode(clusterId: Long, partitionId: Long, newNodeId: Long)
    suspend fun clusterGet(clusterId: Long): ClusterStatus
    suspend fun clusterList(offset: Long, limit: Long, filterManagerId: AccountId? = null): ResultList<ClusterStatus>
    suspend fun clusterDistributeRevenues(clusterId: Long)

}