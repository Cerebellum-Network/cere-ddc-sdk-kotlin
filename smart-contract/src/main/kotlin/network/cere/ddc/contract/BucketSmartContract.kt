package network.cere.ddc.contract

import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.query.command.*
import network.cere.ddc.contract.query.contract.*
import java.net.ConnectException

class BucketSmartContract(
    private val client: SmartContractClient,
    private val bucketContractConfig: BucketContractConfig,
    contractBuckets: ContractBuckets = ContractBuckets(client, bucketContractConfig),
    contractClusters: ContractClusters = ContractClusters(client, bucketContractConfig),
    contractNodes: ContractNodes = ContractNodes(client, bucketContractConfig),
    contractAdmins: ContractAdmins = ContractAdmins(client, bucketContractConfig),
    contractBillings: ContractBillings = ContractBillings(client, bucketContractConfig)
) : Buckets by contractBuckets, Clusters by contractClusters, Nodes by contractNodes, Admins by contractAdmins,
    Billings by contractBillings, AutoCloseable by client {

    companion object {
        suspend fun buildAndConnect(
            config: BlockchainConfig,
            bucketContractConfig: BucketContractConfig
        ): BucketSmartContract {
            return BucketSmartContract(config, bucketContractConfig)
                .also { if (!it.connect()) throw ConnectException("Could not connect to blockchain: '${config.wsUrl}'") }
        }

        fun mock(
            config: BlockchainConfig,
            bucketContractConfig: BucketContractConfig
        ): BucketSmartContract {
            return BucketSmartContract(config, bucketContractConfig)
        }
    }

    constructor(
        config: BlockchainConfig,
        bucketContractConfig: BucketContractConfig
    ) : this(SmartContractClient(config), bucketContractConfig)

    suspend fun connect(): Boolean {
        bucketContractConfig.init()
        return client.connect()
    }
}