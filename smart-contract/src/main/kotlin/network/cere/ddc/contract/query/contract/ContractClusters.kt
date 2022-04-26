package network.cere.ddc.contract.query.contract

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.writer.ListWriter
import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.event.ClusterCreatedEventReader
import network.cere.ddc.contract.mapping.event.ClusterNodeReplacedEventReader
import network.cere.ddc.contract.mapping.response.ClusterStatusReader
import network.cere.ddc.contract.mapping.response.ResultListReader
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.BucketSmartContractError
import network.cere.ddc.contract.model.event.ClusterCreatedEvent
import network.cere.ddc.contract.model.event.ClusterNodeReplacedEvent
import network.cere.ddc.contract.model.response.ClusterStatus
import network.cere.ddc.contract.model.response.ResultList
import network.cere.ddc.contract.query.command.Clusters

class ContractClusters(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    Clusters {

    private val nodeIdsWriter = ListWriter(ScaleCodecWriter.ULONG32)
    private val clusterStatusListReader = ResultListReader(ClusterStatusReader)

    override suspend fun clusterCreate(
        value: Balance,
        manager: AccountId,
        partitionCount: Long,
        nodeIds: List<Long>,
        clusterParams: String
    ): ClusterCreatedEvent {
        val event = client.callTransaction(contractConfig.getMethodHashByName("cluster_create"), value.value) {
            write(AccountIdScale, manager)
            writeUint32(partitionCount)
            write(nodeIdsWriter, nodeIds)
            writeString(clusterParams)
        }

        return event.read(ClusterCreatedEventReader)
    }

    override suspend fun clusterReserveResource(clusterId: Long, amount: Long) {
        client.callTransaction(contractConfig.getMethodHashByName("cluster_reserve_resource")) {
            writeUint32(clusterId)
            writeUint32(amount)
        }
    }

    override suspend fun clusterReplaceNode(
        clusterId: Long,
        partitionId: Long,
        newNodeId: Long
    ): ClusterNodeReplacedEvent {
        val event = client.callTransaction(contractConfig.getMethodHashByName("cluster_replace_node")) {
            writeUint32(clusterId)
            writeUint32(partitionId)
            writeUint32(newNodeId)
        }

        return event.read(ClusterNodeReplacedEventReader)
    }

    override suspend fun clusterGet(clusterId: Long): ClusterStatus {
        val response = client.call(contractConfig.getMethodHashByName("cluster_get")) {
            writeUint32(clusterId)
        }

        return response.checkError(BucketSmartContractError.values()).read(ClusterStatusReader)
    }

    override suspend fun clusterList(
        offset: Long,
        limit: Long,
        filterManagerId: AccountId?
    ): ResultList<ClusterStatus> {
        val response = client.call(contractConfig.getMethodHashByName("cluster_list")) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, filterManagerId)
        }

        return response.read(clusterStatusListReader)
    }

    override suspend fun clusterDistributeRevenues(clusterId: Long) {
        client.callTransaction(contractConfig.getMethodHashByName("cluster_distribute_revenues")) {
            writeUint32(clusterId)
        }
    }

}