package network.cere.ddc.nft.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.core.extension.retry
import network.cere.ddc.core.http.HttpSecurity
import network.cere.ddc.core.http.defaultHttpClient
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.config.TransportClientConfig
import network.cere.ddc.nft.exception.NftException
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.reflect.KClass

class HttpTransportClient(
    private val scheme: Scheme,
    private val config: TransportClientConfig,
    httpClient: HttpClient = defaultHttpClient(),
    private val schemas: Map<String, KClass<out Metadata>> = Metadata.getAllMetadata()
) : TransportClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val METADATA_SCHEMA_NAME_HEADER = "Nft-Standard"
        const val CONTENT_PATH_HEADER = "Content-Path"
        const val NFT_STANDARD_HEADER = "Nft-Standard"
        const val BASIC_NFT_URL = "%s/api/rest/nfts/%s"
    }

    private val client: HttpClient
    private val nodeFlag = AtomicInteger()
    private val objectMapper = jacksonObjectMapper()
    private val nodeAddresses =
        java.util.concurrent.ConcurrentHashMap(config.trustedNodes.associate { it.address to it.id })

    init {
        if (config.trustedNodes.isEmpty()) {
            throw IllegalStateException("Trusted node list is empty")
        }

        client = httpClient.config {
            install(HttpTimeout) {
                requestTimeoutMillis = config.requestTimeout.toMillis()
                connectTimeoutMillis = config.connectTimeout.toMillis()
            }

            install(JsonFeature) {
                this.serializer = JacksonSerializer()
            }

            install(HttpSecurity) {
                expiresAfter = config.requestExpiration
                scheme = this@HttpTransportClient.scheme
                nodeAddresses = this@HttpTransportClient.nodeAddresses
            }

            followRedirects = false
            expectSuccess = false
        }
    }

    override suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath =
        storeData("$BASIC_NFT_URL/assets", nftId, data) { headers { set(CONTENT_PATH_HEADER, name) } }

    override suspend fun readAsset(nftId: String, cid: String): ByteArray =
        readData("$BASIC_NFT_URL/assets/%s", nftId, cid)

    override suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath =
        storeData("$BASIC_NFT_URL/metadata", nftId, metadata) {
            contentType(ContentType.Application.Json)
            headers {
                set(NFT_STANDARD_HEADER, metadata.schema)
            }
        }

    override suspend fun readMetadata(nftId: String, cid: String): Metadata {
        var schemaName = ""
        val bytes: ByteArray = readData("$BASIC_NFT_URL/metadata/%s", nftId, cid) {
            schemaName =
                requireNotNull(it.headers[METADATA_SCHEMA_NAME_HEADER]) { "Schema name header $METADATA_SCHEMA_NAME_HEADER is absent" }
                    .lowercase()
        }

        return requireNotNull(schemas[schemaName]) { "Unsupported metadata schema name: $schemaName" }.let {
            @Suppress("BlockingMethodInNonBlockingContext")
            objectMapper.readValue(bytes, it.java)
        }
    }

    override suspend fun storeEdek(nftId: String, cid: String, edek: Edek): Edek =
        storeData("$BASIC_NFT_URL/metadata/$cid/edek", nftId, edek.copy(metadataCid = cid)) {
            contentType(ContentType.Application.Json)
        }

    override suspend fun readEdek(nftId: String, cid: String, publicKeyHex: String): Edek =
        readData("$BASIC_NFT_URL/metadata/%s/edek/$publicKeyHex", nftId, cid)

    override fun close() {
        client.close()
    }

    private suspend inline fun <reified T> storeData(
        path: String, nftId: String, data: Any, block: HttpRequestBuilder.() -> Unit = {}
    ): T = try {
        val node = getNode()
        retry(
            times = config.retryTimes,
            backOff = config.retryBackOff,
            { it is NftException || it is ConnectException }) {
            val response = client.request<HttpResponse>(String.format(path, node.address, nftId)) {
                method = HttpMethod.Put
                body = data
                block()
            }

            if (HttpStatusCode.Created != response.status) {
                throw NftException(
                    "Invalid response status for node=$node. Response: status='${response.status}' body='${response.receive<String>()}'"
                )
            }

            return response.receive()
        }
    } catch (e: Exception) {
        nodeFlag.incrementAndGet()
        throw e
    }

    private suspend inline fun <reified T> readData(
        path: String, nftId: String, cid: String, validation: (HttpResponse) -> (Unit) = {}
    ): T = try {
        val node = getNode()
        retry(
            times = config.retryTimes,
            backOff = config.retryBackOff,
            { it is NftException || it is ConnectException }) {
            val response = client.get<HttpResponse>(String.format(path, node.address, nftId, cid))

            return when (response.status) {
                HttpStatusCode.OK -> {
                    response
                }
                HttpStatusCode.MultipleChoices -> {
                    response.receive<List<Node>>()
                        .onEach { nodeAddresses[it.address] = it.id }
                        .let { redirectToNodes(path, nftId, cid, it) }
                }
                else -> {
                    throw NftException("Invalid response status for node=$node. Response: status=${response.status} body='${response.receive<String>()}'")
                }
            }.also(validation).receive()
        }
    } catch (e: Exception) {
        nodeFlag.incrementAndGet()
        throw e
    }

    private suspend fun redirectToNodes(
        path: String, nftId: String, cid: String, redirectNodeAddresses: List<Node>
    ): HttpResponse {
        redirectNodeAddresses.forEach { node ->
            val response = client.get<HttpResponse>(String.format(path, node.address, nftId, cid))

            if (response.status == HttpStatusCode.OK) {
                return response
            }

            logger.warn(
                "Couldn't get data from redirected node={}. Response: status='{}' body='{}'",
                node,
                response.status,
                response.receive<String>()
            )
        }

        throw NftException("Couldn't get asset from nodes: $redirectNodeAddresses")
    }

    private fun getNode() = config.trustedNodes[abs(nodeFlag.get()) % config.trustedNodes.size]
}