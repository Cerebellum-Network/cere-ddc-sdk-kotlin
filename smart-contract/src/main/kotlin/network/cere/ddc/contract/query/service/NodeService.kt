package network.cere.ddc.contract.query.service

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.NodeStatusReader
import network.cere.ddc.contract.mapping.ResultListReader
import network.cere.ddc.contract.model.*
import network.cere.ddc.contract.query.commander.NodeCommander

class NodeService(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    NodeCommander {

    private val nodeStatusListReader = ResultListReader(NodeStatusReader)

    override suspend fun nodeCreate(value: Balance, rentPerMonth: Balance, nodeParams: String, capacity: Long): Long {
        val response = client.callTransaction(contractConfig.nodeCreateHash, value.value) {
            write(BalanceScale, rentPerMonth)
            writeString(nodeParams)
            writeUint32(capacity)
        }

        return response.readUint32()
    }

    override suspend fun nodeGet(nodeId: Long): NodeStatus {
        val response = client.call(contractConfig.nodeGetHash) {
            writeUint32(nodeId)
        }

        return response.checkError(BucketSmartContractError.values()).read(NodeStatusReader)
    }

    override suspend fun nodeList(offset: Long, limit: Long, filterProviderId: AccountId?): ResultList<NodeStatus> {
        val response = client.call(contractConfig.nodeListHash) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, filterProviderId)
        }

        return response.checkError(BucketSmartContractError.values()).read(nodeStatusListReader)
    }
}