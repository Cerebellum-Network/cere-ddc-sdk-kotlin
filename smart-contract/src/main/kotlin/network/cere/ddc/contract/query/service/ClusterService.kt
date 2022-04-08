package network.cere.ddc.contract.query.service

import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.writer.ListWriter
import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.ClusterStatusReader
import network.cere.ddc.contract.mapping.ResultListReader
import network.cere.ddc.contract.model.*
import network.cere.ddc.contract.query.commander.ClusterCommander

class ClusterService(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    ClusterCommander {

    private val nodeIdsWriter = ListWriter(ScaleCodecWriter.ULONG32)
    private val clusterStatusListReader = ResultListReader(ClusterStatusReader)

    override suspend fun clusterCreate(
        value: Balance,
        manager: AccountId,
        partitionCount: Long,
        nodeIds: List<Long>,
        clusterParams: String
    ): Long {
        val response = client.callTransaction(contractConfig.clusterCreateHash, value.value) {
            write(AccountIdScale, manager)
            writeUint32(partitionCount)
            write(nodeIdsWriter, nodeIds)
            writeString(clusterParams)
        }

        return response.readUint32()
    }

    override suspend fun clusterReserveResource(clusterId: Long, amount: Long) {
        client.callTransaction(contractConfig.clusterReserveResourceHash) {
            writeUint32(clusterId)
            writeUint32(amount)
        }
    }

    override suspend fun clusterReplaceNode(clusterId: Long, partitionId: Long, newNodeId: Long) {
        client.callTransaction(contractConfig.clusterReplaceNodeHash) {
            writeUint32(clusterId)
            writeUint32(partitionId)
            writeUint32(newNodeId)
        }
    }

    override suspend fun clusterGet(clusterId: Long): ClusterStatus {
        val response = client.call(contractConfig.clusterGetHash) {
            writeUint32(clusterId)
        }

        return response.checkError(BucketSmartContractError.values()).read(ClusterStatusReader)
    }

    override suspend fun clusterList(
        offset: Long,
        limit: Long,
        filterManagerId: AccountId?
    ): ResultList<ClusterStatus> {
        val response = client.call(contractConfig.clusterListHash) {
            writeUint32(offset)
            writeUint32(limit)
            write(AccountIdScale, filterManagerId)
        }

        return response.read(clusterStatusListReader)
    }

    override suspend fun clusterDistributeRevenues(clusterId: Long) {
        client.callTransaction(contractConfig.clusterDistributeRevenuesHash) {
            writeUint32(clusterId)
        }
    }

}