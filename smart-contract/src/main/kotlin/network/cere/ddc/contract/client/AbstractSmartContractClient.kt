package network.cere.ddc.contract.client

import com.debuggor.schnorrkel.sign.KeyPair
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.api.RpcCall
import io.emeraldpay.polkaj.api.StandardCommands
import io.emeraldpay.polkaj.apiws.PolkadotWsApi
import io.emeraldpay.polkaj.json.ContractCallRequestJson
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scaletypes.Extrinsic
import io.emeraldpay.polkaj.scaletypes.Extrinsic.TransactionInfo
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel
import io.emeraldpay.polkaj.ss58.SS58Codec
import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.tx.ExtrinsicContext
import io.emeraldpay.polkaj.tx.ExtrinsicSigner
import io.emeraldpay.polkaj.types.Address
import io.emeraldpay.polkaj.types.ByteData
import kotlinx.coroutines.future.await
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.mapping.RawContractCallExtrinsicWriter
import network.cere.ddc.contract.mapping.RawContractCallWriter
import network.cere.ddc.contract.model.ContractCallResponse
import network.cere.ddc.contract.model.RawContractCallExtrinsic
import network.cere.ddc.core.extension.hexToBytes
import org.komputing.khex.extensions.toNoPrefixHexString
import java.io.ByteArrayOutputStream
import java.math.BigInteger


abstract class AbstractSmartContractClient(config: ContractConfig) : AutoCloseable {

    private companion object {
        val MAX_GAS_LIMIT_TRANSACTION = BigInteger.valueOf(1280000000000)
    }

    //ToDo probably it can be WS Api, check and implement
    private val api = PolkadotWsApi
        .newBuilder()
        .objectMapper(jacksonObjectMapper().registerModule(PolkadotModule()))
        .connectTo(config.wsUrl)
        .build()

    private val signer = ExtrinsicSigner(RawContractCallWriter)
    private val contractAddress = Address.from(config.contractAddress)
    private val keyPair: Schnorrkel.KeyPair
    private val operationalWallet: Address

    init {
        val kp = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())

        keyPair = Schnorrkel.KeyPair(kp.publicKey.toPublicKey(), kp.privateKey.toPrivateKey())
        operationalWallet =
            Address.from(SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey))
    }

    suspend fun connect(): Boolean = api.connect().await()

    override fun close() {
        api.close()
    }

    protected fun call(hash: String, paramsApply: ScaleCodecWriter.() -> Unit) = ContractCallRequestJson().apply {
        origin = operationalWallet
        dest = contractAddress
        value = 0
        gasLimit = 4999999999999
        inputData = ByteData(writerToBytes(hash, paramsApply))
    }.let { api.execute(RpcCall.create(ContractCallResponse::class.java, "contracts_call", it)) }
        .thenApply {
            val reader = ScaleCodecReader(it.success?.data?.hexToBytes())
            val resultCode = reader.readUByte()

            if (resultCode == 0) {
                reader
            } else {
                TODO("Implement error parsing by code")
            }
        }

    private inline fun writerToBytes(hash: String = "", paramsApply: ScaleCodecWriter.() -> Unit) =
        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use { it.paramsApply() }
            buffer.toByteArray()
        }.let { hash.hexToBytes() + it }

    //ToDO implement more flexible gasLimit/autoprediction(right now always MAX gasLimit)
    suspend fun callTransaction(hash: String, paramsApply: ScaleCodecWriter.() -> Unit) {
        val data = writerToBytes(hash, paramsApply)
        val call = RawContractCallExtrinsic(data, contractAddress, MAX_GAS_LIMIT_TRANSACTION)

        //ToDo probably we don't have to execute it on every transaction
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()

        val transactionInfo = TransactionInfo().apply {
            sender = operationalWallet
            signature = Extrinsic.SR25519Signature(signer.sign(context, call, keyPair))
            era = context.eraHeight.toInt()
            nonce = context.nonce
            tip = context.tip
        }
        val extrinsic = Extrinsic<RawContractCallExtrinsic>().apply {
            this.call = call
            tx = transactionInfo
        }

        writerToBytes { write(RawContractCallExtrinsicWriter, extrinsic) }
            .also { println(it.toNoPrefixHexString()) }
            .let { api.execute(StandardCommands.getInstance().authorSubmitExtrinsic(ByteData(it))) }
            .await()
    }
}