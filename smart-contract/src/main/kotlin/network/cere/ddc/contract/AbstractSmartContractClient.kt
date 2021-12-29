package network.cere.ddc.contract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.api.RpcCall
import io.emeraldpay.polkaj.apihttp.PolkadotHttpApi
import io.emeraldpay.polkaj.json.ContractCallRequestJson
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.types.Address
import io.emeraldpay.polkaj.types.ByteData
import network.cere.ddc.contract.config.BlockchainConfig
import network.cere.ddc.contract.model.ContractCallResponse
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.extension.toHex
import java.io.ByteArrayOutputStream

abstract class AbstractSmartContractClient(
    blockchainConfig: BlockchainConfig,
    address: String,
) : AutoCloseable {

    private val api = PolkadotHttpApi
        .newBuilder()
        .objectMapper(jacksonObjectMapper().registerModule(PolkadotModule()))
        .connectTo(blockchainConfig.httpUrl)
        .build()
    private val operationalWallet = Address.from(blockchainConfig.operationalWallet)
    private val contractAddress = Address.from(address)

    override fun close() {
        api.close()
    }

    protected inline fun call(hash: String, paramsApply: ScaleCodecWriter.() -> Unit) =
        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use { it.paramsApply() }
            buffer.toByteArray()
        }
            .toHex(false)
            .let { call(hash + it) }

    protected fun call(hash: String) = ContractCallRequestJson().apply {
        origin = operationalWallet
        dest = contractAddress
        value = 0
        gasLimit = 1 //ToDo set correct gasLimit
        inputData = ByteData.from(hash)
    }.let { api.execute(RpcCall.create(ContractCallResponse::class.java, "contracts_call", it)) }
        .thenApply { ScaleCodecReader(it.success?.data?.hexToBytes()) }
}