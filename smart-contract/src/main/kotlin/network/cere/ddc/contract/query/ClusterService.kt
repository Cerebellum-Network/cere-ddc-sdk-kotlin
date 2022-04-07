package network.cere.ddc.contract.query

import network.cere.ddc.contract.blockchain.SmartContractClient
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.ClusterStatus
import network.cere.ddc.contract.model.ResultList
import network.cere.ddc.contract.query.commander.ClusterCommander

class ClusterService(private val client: SmartContractClient) : ClusterCommander {
    //payable
    override suspend fun clusterCreate(
        manager: AccountId,
        partitionCount: Long,
        nodeIds: List<Long>,
        clusterParams: String
    ): Long {
        TODO("Not yet implemented")
    }

    override suspend fun clusterReserveResource(clusterId: Long, amount: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun clusterReplaceNode(clusterId: Long, partitionId: Long, newNodeId: Long) {
        TODO("Not yet implemented")
    }

    //result
    override suspend fun clusterGet(clusterId: Long): ClusterStatus {
        TODO("Not yet implemented")
    }

    override suspend fun clusterList(
        offset: Long,
        limit: Long,
        filterManagerId: AccountId?
    ): ResultList<ClusterStatus> {
        TODO("Not yet implemented")
    }

    override suspend fun clusterDistributeRevenues(clusterId: Long) {
        TODO("Not yet implemented")
    }

}