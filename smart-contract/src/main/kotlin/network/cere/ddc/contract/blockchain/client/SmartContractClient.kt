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
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.await
import network.cere.ddc.contract.blockchain.BlockchainConfig
import network.cere.ddc.contract.blockchain.mapping.reader.ContractCallEventReader
import network.cere.ddc.contract.blockchain.mapping.reader.EventReader
import network.cere.ddc.contract.blockchain.mapping.reader.MetadataReader
import network.cere.ddc.contract.blockchain.mapping.reader.skip.SkipReaderGenerator
import network.cere.ddc.contract.blockchain.mapping.writer.RawContractCallExtrinsicWriter
import network.cere.ddc.contract.blockchain.mapping.writer.RawContractCallWriter
import network.cere.ddc.contract.blockchain.model.*
import network.cere.ddc.core.extension.hexToBytes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.util.concurrent.TimeUnit


class SmartContractClient(private val config: BlockchainConfig) : AutoCloseable {

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(SmartContractClient::class.java)
    }

    private val defaultTypeFiles =
        listOf("${File.separator}default_types.json", "${File.separator}cere_custom_types.json")
    private val api = PolkadotWsApi.newBuilder().objectMapper(JACKSON).connectTo(config.wsUrl).build()

    private val contractAddress = Address.from(config.contractAddressHex)
    private val keyPair = KeyPair.fromPrivateKey(config.privateKeyHex.hexToBytes())
        .let { Schnorrkel.KeyPair(it.publicKey.toPublicKey(), it.privateKey.toPrivateKey()) }
    private val operationalWallet =
        Address.from(SS58Codec.getInstance().encode(SS58Type.Network.SUBSTRATE, keyPair.publicKey))

    private lateinit var metadata: ChainMetadata
    private lateinit var skipReaderGenerator: SkipReaderGenerator

    suspend fun connect(): Boolean {
        LOGGER.info("Try connect to blockchain wsUrl='{}'", config.wsUrl)
        val connected = api.connect().await()
        LOGGER.info("Smart contract client connected={}", connected)

        if (connected) {
            LOGGER.info("Read blockchain metadata")
            metadata = api.execute(StandardCommands.getInstance().stateMetadata())
                .await()
                .let { ScaleCodecReader(it.bytes).read(MetadataReader) }
        } else {
            return connected
        }

        skipReaderGenerator = withContext(Dispatchers.IO) {
            val defaultTypeNode = defaultTypeFiles.fold(JACKSON.createObjectNode()) { node, file ->
                javaClass.getResourceAsStream(file)
                    ?.use { JACKSON.readTree(it) }
                    ?.let { JACKSON.readerForUpdating(node).readValue(it) }
            }

            config.typeFiles.fold(defaultTypeNode) { node, path ->
                JACKSON.readerForUpdating(node).readValue(JACKSON.readTree(Files.readAllBytes(path)))
            }
        }.let { SkipReaderGenerator(it) }

        return connected
    }

    override fun close() {
        api.close()
    }

    suspend fun call(
        hashHex: String,
        paramsApply: ScaleCodecWriter.() -> Unit = {}
    ): ScaleCodecReader = ScaleCodecReader(callReadOnly(hashHex, 0L, paramsApply).success?.data?.hexToBytes())

    private suspend fun callReadOnly(hashHex: String, value: Long, paramsApply: ScaleCodecWriter.() -> Unit = {}) =
        ContractCallRequestJson().apply {
            origin = operationalWallet
            dest = contractAddress
            gasLimit = MAX_GAS_LIMIT_READ
            inputData = ByteData(writerToBytes(hashHex, paramsApply))
            this.value = value
        }.let { api.execute(RpcCall.create(ContractCallResponse::class.java, CONTRACT_CALL_READ_COMMAND, it)) }
            .await()

    suspend fun callTransaction(
        hashHex: String,
        predictGasLimit: Boolean,
        value: BigInteger = BigInteger.ZERO,
        paramsApply: ScaleCodecWriter.() -> Unit = {}
    ): ScaleCodecReader {
        val gasLimit = if (predictGasLimit) {
            LOGGER.debug("Predict gas limit for function={}", hashHex)
            callReadOnly(hashHex, value.toLong(), paramsApply).success?.gasConsumed
                ?.let { BigInteger.valueOf(it) }
                ?: throw RuntimeException("Couldn't predict gas limit")
        } else {
            MAX_GAS_LIMIT_TRANSACTION
        }

        val call = ContractExtrinsicCall(
            data = writerToBytes(hashHex, paramsApply),
            contractAddress = contractAddress,
            gasLimit = gasLimit,
            value = value
        )

        val extrinsic = buildExtrinsic(call)
        val byteData = ByteData(writerToBytes { write(RawContractCallExtrinsicWriter, extrinsic) })
        val blockHash = sendExtrinsic(byteData)

        val contractCallEvent = findEvents(blockHash, byteData).also { events ->
            events.firstOrNull { it.moduleId == 0 && it.eventId == 0 }
                ?: throw RuntimeException("Event was not found. Contract transaction failed")
        }
        val eventData = contractCallEvent.mapNotNull { it.event }.firstOrNull()?.data ?: byteArrayOf()

        return ScaleCodecReader(eventData)
    }

    private suspend fun sendExtrinsic(data: ByteData) = coroutineScope {
        val subscribeCall = SubscribeCall.create(
            JsonNode::class.java,
            SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND,
            UNSUBSCRIBE_TRANSACTION_EVENTS_COMMAND,
            data
        )
        val blockHashChannel = Channel<String>(1)

        LOGGER.debug("Execute {} with data={}", SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND, data)
        val subscription =
            api.subscribe(subscribeCall).orTimeout(config.timeout.toMillis(), TimeUnit.MILLISECONDS).await()
        LOGGER.debug("Returned subscription {} with data={}", SEND_TRANSACTION_AND_SUBSCRIBE_COMMAND, data)

        subscription.use {
            subscription.handler { event ->
                event.result.get("finalized")?.asText()
                    ?.also { blockHash -> launch { blockHashChannel.send(blockHash) } }
            }

            withTimeout(config.timeout.toMillis()) {
                blockHashChannel.receive()
                    .also { LOGGER.debug("Finalized extrinsic for data={} in block with hash={}", data, it) }
            }
        }

    }

    private suspend fun findEvents(blockHash: String, byteData: ByteData): List<EventRecord<ContractCallEvent>> {
        LOGGER.debug("Execute chain_getBlock for block hash={}", blockHash)
        val index = api.execute(StandardCommands.getInstance().getBlock(Hash256.from(blockHash)))
            .thenApply {
                LOGGER.debug("Response chain_getBlock for block hash={}", blockHash)
                it.block.extrinsics.indexOf(byteData)
            }
            .orTimeout(config.timeout.toMillis(), TimeUnit.MILLISECONDS)

        LOGGER.debug("Execute {} for block hash={}", STATE_GET_STORAGE_READ_COMMAND, blockHash)
        val eventsData = RpcCall
            .create(ByteData::class.java, STATE_GET_STORAGE_READ_COMMAND, SYSTEM_EVENTS_KEY_STATE_STORAGE, blockHash)
            .let { api.execute(it).orTimeout(config.timeout.toMillis(), TimeUnit.MILLISECONDS).await().bytes }
        LOGGER.debug("Response {} for block hash={}", STATE_GET_STORAGE_READ_COMMAND, blockHash)

        return ScaleCodecReader(eventsData)
            .read(ListReader(EventReader(ContractCallEventReader, metadata, skipReaderGenerator)))
            .filter { it.id == index.await().toLong() }
    }

    private suspend fun buildExtrinsic(contractExtrinsicCall: ContractExtrinsicCall): Extrinsic<ContractExtrinsicCall> {
        val context = ExtrinsicContext.newAutoBuilder(operationalWallet, api).await().build()

        val transactionInfo = Extrinsic.TransactionInfo().apply {
            sender = operationalWallet
            signature = Extrinsic
                .SR25519Signature(ExtrinsicSigner(RawContractCallWriter).sign(context, contractExtrinsicCall, keyPair))
            era = context.eraHeight.toInt()
            nonce = context.nonce
            tip = context.tip
        }

        return Extrinsic<ContractExtrinsicCall>().apply {
            call = contractExtrinsicCall
            tx = transactionInfo
        }
    }

    private inline fun writerToBytes(hash: String = "", paramsApply: ScaleCodecWriter.() -> Unit) =
        ByteArrayOutputStream().use { buffer ->
            ScaleCodecWriter(buffer).use { it.paramsApply() }
            buffer.toByteArray()
        }.let { hash.hexToBytes() + it }
}

