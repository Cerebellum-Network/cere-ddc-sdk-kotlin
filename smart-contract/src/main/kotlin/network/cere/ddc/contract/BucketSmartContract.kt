package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.query.commander.*
import network.cere.ddc.contract.query.service.*
import java.net.ConnectException

//ToDo make dynamic EventReader and request builder by parsing JSON ABI
class BucketSmartContract(
    client: SmartContractClient,
    bucketContractConfig: BucketContractConfig,
) : BucketCommander by BucketService(client, bucketContractConfig),
    ClusterCommander by ClusterService(client, bucketContractConfig),
    NodeCommander by NodeService(client, bucketContractConfig),
    AdminCommander by AdminService(client, bucketContractConfig),
    PermissionCommander by PermissionService(client, bucketContractConfig),
    BillingCommander by BillingService(client, bucketContractConfig),
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