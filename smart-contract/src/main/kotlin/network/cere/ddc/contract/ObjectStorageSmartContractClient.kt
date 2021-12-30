package network.cere.ddc.contract

import io.emeraldpay.polkaj.scale.reader.ListReader
import io.emeraldpay.polkaj.scale.reader.StringReader
import kotlinx.coroutines.future.await
import network.cere.ddc.contract.config.BlockchainConfig
import network.cere.ddc.contract.config.ObjectStorageSmartContractConfig
import network.cere.ddc.contract.exception.ObjectStorageSmartContractException
import network.cere.ddc.contract.model.BucketStatus
import network.cere.ddc.core.extension.hexPrefix
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ObjectStorageSmartContractClient(
    blockchainConfig: BlockchainConfig,
    private val smartContractConfig: ObjectStorageSmartContractConfig
) : AbstractSmartContractClient(blockchainConfig, smartContractConfig.address), ObjectStorageSmartContract {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getBucketStatus(bucketId: Long, providerId: String): BucketStatus {
        val response = call(smartContractConfig.getRentEndMsFunctionName) {
            writeUint32(bucketId)
            writeByteArray(providerId.hexPrefix(false).toByteArray())
        }.await()

        return BucketStatus(
            providerId = response.readString(),
            rentEnd = response.readUint32() //ToDo in contract UInt64
                .let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
            writers = response.read(ListReader(StringReader()))
        )
    }

    override suspend fun createBucket(providerId: String) =
        runCatching {
            call(smartContractConfig.getRentEndMsFunctionName + providerId.hexPrefix(false)).await()
        }.getOrThrow("Couldn't create bucket through provider with id='%s'", providerId)
            .readUint32()


    override suspend fun topUpBucket(bucketId: Long) {
        runCatching {
            call(smartContractConfig.getRentEndMsFunctionName) { writeUint32(bucketId) }.await()
        }.getOrThrow("Couldn't top up bucket with id='%d'", bucketId)
    }

    private inline fun <reified T> Result<T>.getOrThrow(errorMessage: String, vararg params: Any): T = this.getOrElse {
        val message = String.format(errorMessage, params)

        logger.warn(message, it)
        throw ObjectStorageSmartContractException(message, it)
    }
}