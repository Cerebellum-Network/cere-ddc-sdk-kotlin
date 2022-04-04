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
import io.emeraldpay.polkaj.scale.ScaleWriter
import io.emeraldpay.polkaj.scaletypes.Extrinsic
import io.emeraldpay.polkaj.scaletypes.Extrinsic.TransactionInfo
import io.emeraldpay.polkaj.scaletypes.ExtrinsicCall
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel
import io.emeraldpay.polkaj.ss58.SS58Codec
import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.tx.AccountRequests
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
import java.math.BigInteger


abstract class AbstractSmartContractClient(config: ContractConfig) : AutoCloseable {

    //ToDo probably it can be WS Api, check and implement
    private val api = PolkadotWsApi
        .newBuilder()
        .objectMapper(jacksonObjectMapper().registerModule(PolkadotModule()))
        .connectTo(config.wsUrl)
        .build()

    private val contractAddress = Address.from(config.contractAddress)
    private val keyPair: Schnorrkel.KeyPair
    private val operationalWallet: Address

    init {
        val kp = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())

        keyPair = Schnorrkel.KeyPair(kp.publicKey.toPublicKey(), kp.privateKey.toPrivateKey())
        operationalWallet =
            Address.from(
                SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey).also { println(it) })
    }

    suspend fun connect(): Boolean = api.connect().await()

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
        val gasLimit = 4999999999999
        val value = 0
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()
        AccountRequests.transfer()
        val scaleWriter = ScaleWriter<StringExtrinsic> { v1, v2 ->
            //v1.writeCompact(100) //message size
            //v1.writeByte(-124) //Determine version (Fixed to V4 for now) ("0x84")

            //User pub Key with prefix
            //v1.writeByte(-1) // ff - pubKey prefix
            //v1.writeUint256(operationalWallet.pubkey)

            //signature etc.

            //v1.writeByteArray(context.era.encode()) // polkadot sends '02' or '01' or '03', is any reason for this and how calculate value from blockhash?
            //v1.writeCompact(context.nonce.toInt())
            //v1.writeCompact(context.tip.value.toInt())

            v1.writeByteArray(byteArrayOf(18, 2)) //1202 - call_index = contracts_call

            //SC PUB Key with prefix
            v1.writeByte(-1) // ff - prefix, looks like index for sending address format
            v1.writeUint256(contractAddress.pubkey) //29ebcfb3296b14ca8ebd454499e9001a4eaa6c77fc6d31240fc3dc4b25f7a392

            val methodName = "0aeb2379".hexToBytes()
            v1.writeCompact(value) //04
            v1.write(ScaleCodecWriter.COMPACT_BIGINT, 1280000000000.toBigInteger()) //a10f
            v1.writeCompact((methodName.size + v2.data.toByteArray().size + 1)) // data size

            //Data
            v1.writeByteArray(methodName) //0aeb2379
            v1.writeAsList(v2.data.toByteArray()) //1c 74657374696e67
        }
        val call = StringExtrinsic("testingKotlin")
        val extrinsic = Extrinsic<StringExtrinsic>().apply {
            this.call = call
            tx = TransactionInfo().apply {
                sender = operationalWallet
                signature =
                    Extrinsic.SR25519Signature(ExtrinsicSigner(scaleWriter).sign(context, call, keyPair))
                era = context.eraHeight.toInt()
                nonce = context.nonce
                tip = context.tip

            }
        }

        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use {
                it.write(ExtrinsicWriterr(scaleWriter), extrinsic)
            }
            buffer.toByteArray()
        }.toNoPrefixHexString()
            .also { println(it) }
            .let { api.execute(StandardCommands.getInstance().authorSubmitExtrinsic(ByteData.from(it))) }
            .await().also { println(it) }
    }

    class ExtrinsicWriterr<CALL : ExtrinsicCall?>(private val callScaleWriter: ScaleWriter<CALL>) :
        ScaleWriter<Extrinsic<CALL>> {

        private val txWriter = TransactionInfoWriter()

        override fun write(wrt: ScaleCodecWriter, value: Extrinsic<CALL>) {
            val buf = ByteArrayOutputStream()
            val internal = ScaleCodecWriter(buf)
            val type = 132
            internal.writeByte(type)
            internal.write(txWriter, value.tx)
            internal.write(callScaleWriter, value.call)
            wrt.writeAsList(buf.toByteArray())
        }

        internal class TransactionInfoWriter : ScaleWriter<TransactionInfo> {
            override fun write(wrt: ScaleCodecWriter, value: TransactionInfo) {
                wrt.writeByte(-1)
                wrt.writeUint256(value.sender.pubkey)
                wrt.writeByte(Extrinsic.SignatureType.SR25519.code)
                wrt.writeByteArray(value.signature.value.bytes)
                wrt.writeCompact(value.era)
                wrt.write(ScaleCodecWriter.COMPACT_BIGINT, BigInteger.valueOf(value.nonce))
                wrt.write(ScaleCodecWriter.COMPACT_BIGINT, value.tip.value)
            }
        }
    }
}