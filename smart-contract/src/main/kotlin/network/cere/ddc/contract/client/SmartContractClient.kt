package network.cere.ddc.contract.client

import com.debuggor.schnorrkel.sign.KeyPair
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.emeraldpay.polkaj.api.RpcCall
import io.emeraldpay.polkaj.api.StandardCommands
import io.emeraldpay.polkaj.api.SubscribeCall
import io.emeraldpay.polkaj.apiws.PolkadotWsApi
import io.emeraldpay.polkaj.json.ContractCallRequestJson
import io.emeraldpay.polkaj.json.jackson.PolkadotModule
import io.emeraldpay.polkaj.scale.ScaleCodecReader
import io.emeraldpay.polkaj.scale.ScaleCodecWriter
import io.emeraldpay.polkaj.scale.reader.ListReader
import io.emeraldpay.polkaj.scaletypes.Extrinsic
import io.emeraldpay.polkaj.schnorrkel.Schnorrkel
import io.emeraldpay.polkaj.ss58.SS58Codec
import io.emeraldpay.polkaj.ss58.SS58Type
import io.emeraldpay.polkaj.tx.ExtrinsicContext
import io.emeraldpay.polkaj.tx.ExtrinsicSigner
import io.emeraldpay.polkaj.types.Address
import io.emeraldpay.polkaj.types.ByteData
import io.emeraldpay.polkaj.types.Hash256
import kotlinx.coroutines.future.await
import network.cere.ddc.contract.blockchain.mapping.ExtrinsicEventReader
import network.cere.ddc.contract.blockchain.mapping.MetadataReader
import network.cere.ddc.contract.blockchain.mapping.RawContractCallExtrinsicWriter
import network.cere.ddc.contract.blockchain.mapping.RawContractCallWriter
import network.cere.ddc.contract.blockchain.model.ChainMetadata
import network.cere.ddc.contract.blockchain.model.ContractCallResponse
import network.cere.ddc.contract.blockchain.model.EventRecord
import network.cere.ddc.contract.config.ContractConfig
import network.cere.ddc.core.extension.hexToBytes
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class SmartContractClient(config: ContractConfig) : AutoCloseable {

    private companion object {
        const val CONTRACT_CALL_READ_COMMAND = "contracts_call"
        const val STATE_GET_STORAGE_READ_COMMAND = "state_getStorage"
        const val SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND = "author_submitAndWatchExtrinsic"
        const val UNSUBSCRIBE_TRANSACTION_EVENTS_COMMAND = "author_unwatchExtrinsic"

        const val SYSTEM_EVENTS_KEY_STATE_STORAGE = "0x26aa394eea5630e07c48ae0c9558cef780d41e5e16056765bc8461851072c9d7"

        val MAX_GAS_LIMIT_TRANSACTION: BigInteger = BigInteger.valueOf(1280000000000)
        const val MAX_GAS_LIMIT_READ = 4999999999999L
    }

    private val api = PolkadotWsApi
        .newBuilder()
        .objectMapper(jacksonObjectMapper().registerModule(PolkadotModule()))
        .connectTo(config.wsUrl)
        .build()

    private val signer = ExtrinsicSigner(RawContractCallWriter)

    private val contractAddress = Address.from(config.contractAddress)
    private val keyPair = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())
        .let { Schnorrkel.KeyPair(it.publicKey.toPublicKey(), it.privateKey.toPrivateKey()) }
    private val operationalWallet =
        Address.from(SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey))

    private lateinit var metadata: ChainMetadata

    suspend fun connect(): Boolean {
        val connected = api.connect().await()
        if (connected) {
            metadata = api.execute(StandardCommands.getInstance().stateMetadata())
                .await()
                .let { ScaleCodecReader(it.bytes).read(MetadataReader) }
        }

        return connected
    }

    override fun close() {
        api.close()
    }

    suspend fun call(hashHex: String, paramsApply: ScaleCodecWriter.() -> Unit): ScaleCodecReader =
        ContractCallRequestJson().apply {
            origin = operationalWallet
            dest = contractAddress
            gasLimit = MAX_GAS_LIMIT_READ
            inputData = ByteData(writerToBytes(hashHex, paramsApply))
        }.let { api.execute(RpcCall.create(ContractCallResponse::class.java, CONTRACT_CALL_READ_COMMAND, it)) }
            .thenApply {
                val reader = ScaleCodecReader(it.success?.data?.hexToBytes())
                val resultCode = reader.readUByte()

                if (resultCode == 0) {
                    reader
                } else {
                    TODO("Implement error parsing by code")
                }
            }.await()

    //ToDO implement more flexible gasLimit/autoprediction(right now always MAX gasLimit)
    suspend fun callTransaction(hashHex: String, paramsApply: ScaleCodecWriter.() -> Unit): EventRecord {
        val data = writerToBytes(hashHex, paramsApply)
        val call = RawContractCallExtrinsic(
            data = data,
            contractAddress = contractAddress,
            gasLimit = MAX_GAS_LIMIT_TRANSACTION
        )

        //ToDo probably we don't have to execute it on every transaction
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()
        val extrinsic = buildExtrinsic(call, context)
        val byteData = ByteData(writerToBytes { write(RawContractCallExtrinsicWriter, extrinsic) })
        val blockHash = sendExtrinsic(byteData)

        return findEventResult(blockHash, byteData)
    }

    //ToDo do we need timeout?
    private suspend fun sendExtrinsic(data: ByteData): String {
        val subscribeCall = SubscribeCall.create(
            JsonNode::class.java,
            SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND,
            UNSUBSCRIBE_TRANSACTION_EVENTS_COMMAND,
            data
        )

        return suspendCoroutine { continuation ->
            api.subscribe(subscribeCall).thenApply { sub ->
                sub.handler { event ->
                    event.result.get("finalized")?.asText()
                        ?.also { blockHash -> sub.use { continuation.resume(blockHash) } }
                }
            }
                .handle { _, ex -> continuation.resumeWithException(ex) }
        }
    }

    //ToDo do we need timeout?
    //ToDo event as subscription sign on request for improve speed
    private suspend fun findEventResult(blockHash: String, byteData: ByteData): EventRecord {
        val index = api.execute(StandardCommands.getInstance().getBlock(Hash256.from(blockHash)))
            .thenApply { it.block.extrinsics.indexOf(byteData) }
        val events = api.execute(
            RpcCall.create(
                ByteData::class.java,
                STATE_GET_STORAGE_READ_COMMAND,
                SYSTEM_EVENTS_KEY_STATE_STORAGE,
                blockHash
            )
        ).await()

        return ScaleCodecReader(events.bytes).read(ListReader(ExtrinsicEventReader))
            .first { it.id == index.await().toLong() }
    }

    private fun buildExtrinsic(
        rawContractCallExtrinsic: RawContractCallExtrinsic,
        context: ExtrinsicContext
    ): Extrinsic<RawContractCallExtrinsic> {
        val transactionInfo = Extrinsic.TransactionInfo().apply {
            sender = operationalWallet
            signature = Extrinsic.SR25519Signature(signer.sign(context, rawContractCallExtrinsic, keyPair))
            era = context.eraHeight.toInt()
            nonce = context.nonce
            tip = context.tip
        }

        return Extrinsic<RawContractCallExtrinsic>().apply {
            call = rawContractCallExtrinsic
            tx = transactionInfo
        }
    }

    private inline fun writerToBytes(hash: String = "", paramsApply: ScaleCodecWriter.() -> Unit) =
        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use { it.paramsApply() }
            buffer.toByteArray()
        }.let { hash.hexToBytes() + it }
}
