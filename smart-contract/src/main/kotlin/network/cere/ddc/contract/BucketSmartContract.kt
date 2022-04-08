package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.query.commander.BucketCommander
import network.cere.ddc.contract.query.commander.ClusterCommander
import network.cere.ddc.contract.query.commander.NodeCommander
import network.cere.ddc.contract.query.service.BucketService
import network.cere.ddc.contract.query.service.ClusterService
import network.cere.ddc.contract.query.service.NodeService

class BucketSmartContract(bucketService: BucketService, clusterService: ClusterService, nodeService: NodeService) :
    BucketCommander by bucketService, ClusterCommander by clusterService, NodeCommander by nodeService {

    companion object {
        suspend fun buildAndConnect(
            config: BlockchainConfig,
            bucketContractConfig: BucketContractConfig
        ): BucketSmartContract {
            val client = SmartContractClient(config).also { it.connect() }

            val bucketService = BucketService(client, bucketContractConfig)
            val clusterService = ClusterService(client, bucketContractConfig)
            val nodeService = NodeService(client, bucketContractConfig)

            return BucketSmartContract(bucketService, clusterService, nodeService)
        }
    }
}