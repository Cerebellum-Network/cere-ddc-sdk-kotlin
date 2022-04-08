package network.cere.ddc.contract.query.service

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.BucketStatusReader
import network.cere.ddc.contract.mapping.ResultListReader
import network.cere.ddc.contract.model.*
import network.cere.ddc.contract.query.commander.BucketCommander

class BucketService(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    BucketCommander {

    private val bucketStatusListReader = ResultListReader(BucketStatusReader)

    override suspend fun bucketGet(bucketId: Long): BucketStatus {
        val response = client.call(contractConfig.bucketGetHash) {
            writeUint32(bucketId)
        }

        return response.checkError(BucketSmartContractError.values()).read(BucketStatusReader)
    }

    override suspend fun bucketList(offset: Long, limit: Long, owner: AccountId?): ResultList<BucketStatus> {
        val response = client.call(contractConfig.bucketListHash) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, owner)
        }

        return response.read(bucketStatusListReader)
    }

    override suspend fun bucketCreate(value: Balance, bucketParams: String, clusterId: Long): Long {
        val response = client.callTransaction(contractConfig.bucketCreateHash, value.value) {
            writeString(bucketParams)
            writeUint32(clusterId)
        }

        return response.readUint32()
    }

    override suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long) {
        client.callTransaction(contractConfig.bucketAllocIntoClusterHash) {
            writeUint32(bucketId)
            writeUint32(resource)
        }
    }

    override suspend fun bucketSettlePayment(bucketId: Long) {
        client.callTransaction(contractConfig.bucketSettlePaymentHash) {
            writeUint32(bucketId)
        }
    }
}