package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.query.commander.BucketCommander
import network.cere.ddc.contract.query.commander.ClusterCommander
import network.cere.ddc.contract.query.commander.NodeCommander
import network.cere.ddc.contract.query.service.BucketService
import network.cere.ddc.contract.query.service.ClusterService
import network.cere.ddc.contract.query.service.NodeService
import java.net.ConnectException

//ToDo make dynamic EventReader and request buider by parsing JSON ABI
class BucketSmartContract(
    client: SmartContractClient,
    bucketContractConfig: BucketContractConfig,
) : BucketCommander by BucketService(client, bucketContractConfig),
    ClusterCommander by ClusterService(client, bucketContractConfig),
    NodeCommander by NodeService(client, bucketContractConfig),
    AutoCloseable by client {

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