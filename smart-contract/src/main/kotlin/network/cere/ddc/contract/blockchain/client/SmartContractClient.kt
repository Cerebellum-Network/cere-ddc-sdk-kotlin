package network.cere.ddc.contract.blockchain.client

import com.debuggor.schnorrkel.sign.KeyPair
import com.fasterxml.jackson.databind.JsonNode
import io.emeraldpay.polkaj.api.RpcCall
import io.emeraldpay.polkaj.api.StandardCommands
import io.emeraldpay.polkaj.api.SubscribeCall
import io.emeraldpay.polkaj.apiws.PolkadotWsApi
import io.emeraldpay.polkaj.json.ContractCallRequestJson
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.mapping.IndexedScaleReader
import network.cere.ddc.contract.blockchain.mapping.SkipReaderGenerator
import network.cere.ddc.contract.blockchain.mapping.reader.ContractCallEventReader
import network.cere.ddc.contract.blockchain.mapping.reader.EventReader
import network.cere.ddc.contract.blockchain.mapping.reader.MetadataReader
import network.cere.ddc.contract.blockchain.mapping.writer.RawContractCallExtrinsicWriter
import network.cere.ddc.contract.blockchain.mapping.writer.RawContractCallWriter
import network.cere.ddc.contract.blockchain.model.ChainMetadata
import network.cere.ddc.contract.blockchain.model.ContractCallResponse
import network.cere.ddc.contract.blockchain.model.EventRecord
import network.cere.ddc.contract.blockchain.model.RawContractCallExtrinsic
import network.cere.ddc.core.extension.hexToBytes
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.file.Files
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


//ToDo add logging
class SmartContractClient(private val config: BlockchainConfig) : AutoCloseable {

    private val api = PolkadotWsApi.newBuilder().objectMapper(JACKSON).connectTo(config.wsUrl).build()

    private val contractAddress = Address.from(config.contractAddressHex)
    private val keyPair = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())
        .let { Schnorrkel.KeyPair(it.publicKey.toPublicKey(), it.privateKey.toPrivateKey()) }
    private val operationalWallet =
        Address.from(SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey))

    private lateinit var metadata: ChainMetadata
    private lateinit var skipReaderGenerator: SkipReaderGenerator

    suspend fun connect(): Boolean {
        val connected = api.connect().await()
        if (connected) {
            metadata = api.execute(StandardCommands.getInstance().stateMetadata())
                .await()
                .let { ScaleCodecReader(it.bytes).read(MetadataReader) }
        }

        skipReaderGenerator = withContext(Dispatchers.IO) {
            config.typeFiles.fold(JACKSON.createObjectNode()) { node, path ->
                JACKSON.readerForUpdating(node).readValue(JACKSON.readTree(Files.readAllBytes(path)))
            }
        }.let { SkipReaderGenerator(it) }

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
            .await().let { ScaleCodecReader(it.success?.data?.hexToBytes()) }

    //ToDO implement more flexible gasLimit/autoprediction(right now always MAX gasLimit)
    suspend fun callTransaction(
        hashHex: String,
        value: BigInteger = BigInteger.ZERO,
        paramsApply: ScaleCodecWriter.() -> Unit
    ): ScaleCodecReader {
        val data = writerToBytes(hashHex, paramsApply)
        val call = RawContractCallExtrinsic(
            data = data,
            contractAddress = contractAddress,
            gasLimit = MAX_GAS_LIMIT_TRANSACTION,
            value = value
        )

        //ToDo probably we don't have to execute it on every transaction
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()
        val extrinsic = buildExtrinsic(call, context)
        val byteData = ByteData(writerToBytes { write(RawContractCallExtrinsicWriter, extrinsic) })
        val blockHash = sendExtrinsic(byteData)

        val contractCallEvent = findEvents(ContractCallEventReader, blockHash, byteData)
            .also { events ->
                events.firstOrNull { it.moduleId == 0 && it.eventId == 0 }
                    ?: throw RuntimeException("Event was not found. Contract transaction failed")
            }
        val eventData = contractCallEvent.mapNotNull { it.event }.firstOrNull()?.data ?: byteArrayOf()

        return ScaleCodecReader(eventData)
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
    //ToDo event as subscription sign on request for improving speed
    private suspend fun <T> findEvents(
        reader: IndexedScaleReader<T>,
        blockHash: String,
        byteData: ByteData
    ): List<EventRecord<T>> {
        println(blockHash)
        val index = api.execute(StandardCommands.getInstance().getBlock(Hash256.from(blockHash)))
            .thenApply { it.block.extrinsics.indexOf(byteData) }

        val eventsCall = RpcCall
            .create(ByteData::class.java, STATE_GET_STORAGE_READ_COMMAND, SYSTEM_EVENTS_KEY_STATE_STORAGE, blockHash)
        val eventsData = api.execute(eventsCall).await().bytes

        return ScaleCodecReader(eventsData).read(ListReader(EventReader(reader, metadata, skipReaderGenerator)))
            .filter { it.id == index.await().toLong() }
    }

    private fun buildExtrinsic(
        rawContractCallExtrinsic: RawContractCallExtrinsic,
        context: ExtrinsicContext
    ): Extrinsic<RawContractCallExtrinsic> {
        val transactionInfo = Extrinsic.TransactionInfo().apply {
            sender = operationalWallet
            signature = Extrinsic.SR25519Signature(
                ExtrinsicSigner(RawContractCallWriter).sign(context, rawContractCallExtrinsic, keyPair)
            )
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

