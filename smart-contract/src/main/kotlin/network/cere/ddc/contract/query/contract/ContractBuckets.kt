package network.cere.ddc.contract.query.contract

import network.cere.ddc.contract.BucketContractConfig
import network.cere.ddc.contract.blockchain.client.SmartContractClient
import network.cere.ddc.contract.blockchain.mapping.checkError
import network.cere.ddc.contract.blockchain.mapping.writeNullable
import network.cere.ddc.contract.blockchain.mapping.writeString
import network.cere.ddc.contract.mapping.AccountIdScale
import network.cere.ddc.contract.mapping.event.BucketAllocatedEventReader
import network.cere.ddc.contract.mapping.event.BucketCreatedEventReader
import network.cere.ddc.contract.mapping.response.BucketStatusReader
import network.cere.ddc.contract.mapping.response.ResultListReader
import network.cere.ddc.contract.model.AccountId
import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.BucketSmartContractError
import network.cere.ddc.contract.model.event.BucketAllocatedEvent
import network.cere.ddc.contract.model.event.BucketCreatedEvent
import network.cere.ddc.contract.model.response.BucketStatus
import network.cere.ddc.contract.model.response.ResultList
import network.cere.ddc.contract.query.command.Buckets

class ContractBuckets(private val client: SmartContractClient, private val contractConfig: BucketContractConfig) :
    Buckets {

    private val bucketStatusListReader = ResultListReader(BucketStatusReader)

    override suspend fun bucketGet(bucketId: Long): BucketStatus {
        val response = client.call(contractConfig.getMethodHashByName("bucket_get")) {
            writeUint32(bucketId)
        }

        return response.checkError(BucketSmartContractError.values()).read(BucketStatusReader)
    }

    override suspend fun bucketList(offset: Long, limit: Long, owner: AccountId?): ResultList<BucketStatus> {
        val response = client.call(contractConfig.getMethodHashByName("bucket_list")) {
            writeUint32(offset)
            writeUint32(limit)
            writeNullable(AccountIdScale, owner)
        }

        return response.read(bucketStatusListReader)
    }

    override suspend fun bucketCreate(value: Balance, bucketParams: String, clusterId: Long, predictGasLimit: Boolean): BucketCreatedEvent {
        val event = client.callTransaction(contractConfig.getMethodHashByName("bucket_create"), predictGasLimit, value.value) {
            writeString(bucketParams)
            writeUint32(clusterId)
        }

        return event.read(BucketCreatedEventReader)
    }

    override suspend fun bucketAllocIntoCluster(bucketId: Long, resource: Long, predictGasLimit: Boolean): BucketAllocatedEvent {
        val event = client.callTransaction(contractConfig.getMethodHashByName("bucket_alloc_into_cluster"), predictGasLimit) {
            writeUint32(bucketId)
            writeUint32(resource)
        }

        return event.read(BucketAllocatedEventReader)
    }

    override suspend fun bucketSettlePayment(bucketId: Long, predictGasLimit: Boolean) {
        client.callTransaction(contractConfig.getMethodHashByName("bucket_settle_payment"), predictGasLimit) {
            writeUint32(bucketId)
        }
    }
}