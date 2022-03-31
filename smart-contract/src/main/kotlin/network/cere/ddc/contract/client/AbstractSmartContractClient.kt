package network.cere.ddc.contract.client

import com.debuggor.schnorrkel.sign.KeyPair
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.api.RpcCall
import io.emeraldpay.polkaj.api.StandardCommands
import io.emeraldpay.polkaj.apihttp.PolkadotHttpApi
import io.emeraldpay.polkaj.json.ContractCallRequestJson
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.ScaleWriter
import io.emeraldpay.polkaj.scaletypes.Extrinsic
import io.emeraldpay.polkaj.scaletypes.ExtrinsicCall
import io.emeraldpay.polkaj.scaletypes.ExtrinsicWriter
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel
import io.emeraldpay.polkaj.ss58.SS58Codec
import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.tx.ExtrinsicContext
import io.emeraldpay.polkaj.tx.ExtrinsicSigner
import io.emeraldpay.polkaj.types.Address
import io.emeraldpay.polkaj.types.ByteData
import kotlinx.coroutines.future.await
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.contract.model.ContractCallResponse
import network.cere.ddc.core.extension.hexToBytes
import org.komputing.khex.extensions.toNoPrefixHexString
import java.io.ByteArrayOutputStream

abstract class AbstractSmartContractClient(config: ContractConfig) : AutoCloseable {

    //ToDo probably it can be WS Api, check and implement
    private val api = PolkadotHttpApi
        .newBuilder()
        .objectMapper(jacksonObjectMapper().registerModule(PolkadotModule()))
        .connectTo(config.httpUrl)
        .build()

    private val contractAddress = Address.from(config.contractAddress)
    private val keyPair: Schnorrkel.KeyPair
    private val operationalWallet: Address

    init {
        val kp = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())

        keyPair = Schnorrkel.KeyPair(kp.publicKey.toPublicKey(), kp.privateKey.toPrivateKey())
        operationalWallet =
            Address.from(SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey))
    }

    override fun close() {
        api.close()
    }

    protected inline fun call(hash: String, paramsApply: ScaleCodecWriter.() -> Unit) =
        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use { it.paramsApply() }
            buffer.toByteArray()
        }
            .toNoPrefixHexString()
            .let { call(ByteData.from(hash + it)) }

    protected fun call(data: ByteData) = ContractCallRequestJson().apply {
        origin = operationalWallet
        dest = contractAddress
        value = 0
        gasLimit = 4999999999999
        inputData = data
    }.let { api.execute(RpcCall.create(ContractCallResponse::class.java, "contracts_call", it)) }
        .thenApply {
            println(it)
            val reader = ScaleCodecReader(it.success?.data?.hexToBytes())
            val resultCode = reader.readUByte()

            if (resultCode == 0) {
                reader
            } else {
                TODO("Implement error parsing by code")
            }
        }

    //==========================================EXPERIMENTS=========================================

    class StringExtrinsic(val data: String) : ExtrinsicCall()

    suspend fun some() {
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()
        val scaleWriter = ScaleWriter<StringExtrinsic> { v1, v2 ->
            v1.writeByte(v2.moduleIndex) //00
            v1.writeByte(v2.callIndex) //00
            v1.writeUint256(contractAddress.pubkey) //29ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a392
            v1.writeByteArray("0aeb2379".hexToBytes())
            v1.writeByteArray("1c".hexToBytes())
            v1.writeByteArray(v2.data.toByteArray())
        }
        val extrinsic = Extrinsic<StringExtrinsic>().apply {
            call = StringExtrinsic("testing")
            tx = Extrinsic.TransactionInfo().apply {
                signature =
                    Extrinsic.SR25519Signature(ExtrinsicSigner(scaleWriter).sign(context, call, keyPair))
                nonce = context.nonce
                sender = operationalWallet
            }
        }
//0aeb2379


        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use {
                it.write(ExtrinsicWriter(scaleWriter), extrinsic)
            }
            buffer.toByteArray()
        }.toNoPrefixHexString()
            .also { println(it) }
            .let { api.execute(StandardCommands.getInstance().authorSubmitExtrinsic(ByteData.from(it))) }
            .await().also { println(it) }
    }
}