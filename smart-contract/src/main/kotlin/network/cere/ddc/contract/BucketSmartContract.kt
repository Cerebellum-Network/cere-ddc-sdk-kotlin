package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.SmartContractClient
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.query.BucketService
import network.cere.ddc.contract.query.ClusterService
import network.cere.ddc.contract.query.commander.BucketCommander
import network.cere.ddc.contract.query.commander.ClusterCommander

class BucketSmartContract(bucketService: BucketService, clusterService: ClusterService) :
    BucketCommander by bucketService, ClusterCommander by clusterService {

    companion object {
        suspend fun buildAndConnect(config: ContractConfig): BucketSmartContract {
            val client = SmartContractClient(config).also { it.connect() }

            val bucketService = BucketService(client)
            val clusterService = ClusterService(client)

            return BucketSmartContract(bucketService, clusterService)
        }
    }
}