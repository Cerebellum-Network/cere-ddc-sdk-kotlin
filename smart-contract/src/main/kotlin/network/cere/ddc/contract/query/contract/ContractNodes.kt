package network.cere.ddc.contract.query.contract

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.BalanceScale
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.event.NodeCreatedEventReader
import network.cere.ddc.contract.mapping.response.NodeStatusReader
import network.cere.ddc.contract.mapping.response.ResultListReader
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.BucketSmartContractError
import network.cere.ddc.contract.model.event.NodeCreatedEvent
import network.cere.ddc.contract.model.response.NodeStatus
import network.cere.ddc.contract.model.response.ResultList
import network.cere.ddc.contract.query.command.Nodes

class ContractNodes(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    Nodes {

    private val nodeStatusListReader = ResultListReader(NodeStatusReader)

    override suspend fun nodeCreate(
        value: Balance,
        rentPerMonth: Balance,
        nodeParams: String,
        capacity: Long
    ): NodeCreatedEvent {
        val event = client.callTransaction(contractConfig.getMethodHashByName("node_create"), value.value) {
            write(BalanceScale, rentPerMonth)
            writeString(nodeParams)
            writeUint32(capacity)
        }

        return event.read(NodeCreatedEventReader)
    }

    override suspend fun nodeGet(nodeId: Long): NodeStatus {
        val response = client.call(contractConfig.getMethodHashByName("node_get")) {
            writeUint32(nodeId)
        }

        return response.checkError(BucketSmartContractError.values()).read(NodeStatusReader)
    }

    override suspend fun nodeList(offset: Long, limit: Long, filterProviderId: AccountId?): ResultList<NodeStatus> {
        val response = client.call(contractConfig.getMethodHashByName("node_list")) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, filterProviderId)
        }

        return response.read(nodeStatusListReader)
    }
}