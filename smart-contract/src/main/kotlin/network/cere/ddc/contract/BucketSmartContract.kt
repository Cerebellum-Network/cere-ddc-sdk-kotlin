package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.query.command.*
import network.cere.ddc.contract.query.contract.*
import java.net.ConnectException

//ToDo make dynamic EventReader and request builder by parsing JSON ABI
class BucketSmartContract(
    client: SmartContractClient,
    bucketContractConfig: BucketContractConfig,
    contractBuckets: ContractBuckets = ContractBuckets(client, bucketContractConfig),
    contractClusters: ContractClusters = ContractClusters(client, bucketContractConfig),
    contractNodes: ContractNodes = ContractNodes(client, bucketContractConfig),
    contractAdmins: ContractAdmins = ContractAdmins(client, bucketContractConfig),
    contractPermissions: ContractPermissions = ContractPermissions(client, bucketContractConfig),
    contractBillings: ContractBillings = ContractBillings(client, bucketContractConfig)
) : Buckets by contractBuckets, Clusters by contractClusters, Nodes by contractNodes, Admins by contractAdmins,
    Permissions by contractPermissions, Billings by contractBillings, AutoCloseable by client {

    companion object {
        suspend fun buildAndConnect(
            config: BlockchainConfig,
            bucketContractConfig: BucketContractConfig
        ): BucketSmartContract {
            val client =
                SmartContractClient(config).also { if (!it.connect()) throw ConnectException("Could not connect to blockchain: '${config.wsUrl}'") }

            return BucketSmartContract(client, bucketContractConfig)
        }
    }
}