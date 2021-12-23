package network.cere.ddc.nft.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.core.extension.retry
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.http.HttpSecurity
import network.cere.ddc.core.http.defaultHttpClient
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftConnectionConfig
import network.cere.ddc.nft.exception.*
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.EdekRequest
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.reflect.KClass

class HttpTransportClient(
    private val scheme: Scheme,
    private val config: NftConnectionConfig,
    httpClient: HttpClient = defaultHttpClient(),
    private val schemas: Map<String, KClass<out Metadata>> = Metadata.getAllMetadata()
) : TransportClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val METADATA_SCHEMA_NAME_HEADER = "Nft-Standard"
        const val CONTENT_PATH_HEADER = "Content-Path"
        const val CONTENT_SHA_256_HEADER = "Content-SHA256"
        const val CONTENT_SIGNATURE_HEADER = "Content-Signature"
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

    override suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath {
        try {
            return retry(
                config.retryTimes,
                config.retryBackOff,
                predicateRetry("Couldn't store asset to Nft Storage")
            ) {
                storeData("$BASIC_NFT_URL/assets", nftId, data) {
                    headers {
                        set(CONTENT_PATH_HEADER, name)
                    }
                }
            }
        } catch (e: Exception) {
            throw AssetSaveNftException("Couldn't store asset", e)
        }
    }

    override suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray {
        try {
            return retry(config.retryTimes, config.retryBackOff, predicateRetry("Couldn't read asset in Nft Storage")) {
                readData("$BASIC_NFT_URL/assets/%s", nftId, nftPath)
            }
        } catch (e: Exception) {
            throw AssetReadNftException("Couldn't read asset", e)
        }
    }

    override suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath {
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            val data = objectMapper.writeValueAsBytes(metadata)

            return retry(
                config.retryTimes,
                config.retryBackOff,
                predicateRetry("Couldn't store metadata to Nft Storage")
            ) {
                storeData("$BASIC_NFT_URL/metadata", nftId, data) {
                    headers {
                        set(NFT_STANDARD_HEADER, metadata.schema)
                    }
                }
            }
        } catch (e: Exception) {
            throw MetadataSaveNftException("Couldn't store metadata", e)
        }
    }

    override suspend fun readMetadata(nftId: String, nftPath: NftPath): Metadata {
        try {
            return retry(
                config.retryTimes,
                config.retryBackOff,
                predicateRetry("Couldn't store metadata to Nft Storage")
            ) {
                var schemaName = ""
                val bytes = readData("$BASIC_NFT_URL/metadata/%s", nftId, nftPath) {
                    schemaName = requireNotNull(it.headers[METADATA_SCHEMA_NAME_HEADER]) {
                        "Schema name header $METADATA_SCHEMA_NAME_HEADER is absent"
                    }.lowercase()
                }

                return requireNotNull(schemas[schemaName]) { "Unsupported metadata schema name: $schemaName" }.let {
                    objectMapper.readValue(bytes, it.java)
                }
            }
        } catch (e: Exception) {
            throw MetadataReadNftException("Couldn't read metadata", e)
        }
    }

    override suspend fun storeEdek(nftId: String, metadataNftPath: NftPath, edek: Edek) {
        try {
            return retry(config.retryTimes, config.retryBackOff, predicateRetry("Couldn't store EDEK to Nft Storage")) {
                val cid = parseCid(metadataNftPath)

                @Suppress("BlockingMethodInNonBlockingContext")
                val data = objectMapper.writeValueAsBytes(EdekRequest(cid, edek))

                storeData("$BASIC_NFT_URL/metadata/$cid/edek", nftId, data)
            }
        } catch (e: Exception) {
            throw EdekSaveNftException("Couldn't store edek", e)
        }
    }

    override suspend fun readEdek(nftId: String, metadataNftPath: NftPath, publicKeyHex: String): Edek {
        try {
            return retry(config.retryTimes, config.retryBackOff, predicateRetry("Couldn't read EDEK in Nft Storage")) {
                val bytes = readData("$BASIC_NFT_URL/metadata/%s/$publicKeyHex", nftId, metadataNftPath)

                objectMapper.readValue<EdekRequest>(bytes).edek
            }
        } catch (e: Exception) {
            throw EdekReadNftException("Couldn't read edek", e)
        }
    }

    override fun close() {
        client.close()
    }

    private suspend inline fun readData(
        path: String, nftId: String, nftPath: NftPath, validation: (HttpResponse) -> (Unit) = {}
    ): ByteArray =
        retry(times = config.retryTimes, backOff = config.retryBackOff, { it is NftException }) {
            val node = getNode()
            val cid = parseCid(nftPath)
            val response = client.get<HttpResponse>(String.format(path, node.address, nftId, cid))

            return when (response.status) {
                HttpStatusCode.OK -> {
                    response
                }
                HttpStatusCode.MultipleChoices -> {
                    val redirectNodes: List<Node> = response.receive()
                    redirectNodes.forEach { nodeAddresses[it.address] = it.id }
                    redirectToNodes(path, nftId, cid, redirectNodes)
                }
                else -> {
                    throw NftException("Invalid response status for node=$node. Response: status=${response.status} body='${response.receive<String>()}'")
                }
            }.also(validation).readBytes()
        }


    private suspend inline fun storeData(
        path: String, nftId: String, data: ByteArray, block: HttpRequestBuilder.() -> Unit = {}
    ): NftPath = retry(times = config.retryTimes, backOff = config.retryBackOff, { it is NftException }) {
        val hash = data.sha256()
        val node = getNode()
        val response = client.request<HttpResponse>(String.format(path, node.address, nftId)) {
            method = HttpMethod.Put
            body = data
            headers {
                set(CONTENT_SHA_256_HEADER, hash)
                set(CONTENT_SIGNATURE_HEADER, scheme.sign(hash.toByteArray()))
            }

            block()
        }

        if (HttpStatusCode.Created != response.status) {
            throw NftException(
                "Invalid response status for node=$node. Response: status='${response.status}' body='${response.receive<String>()}'"
            )
        }

        return response.receive()
    }

    private suspend fun redirectToNodes(path: String, nftId: String, cid: String, redirectNodeAddresses: List<Node>): HttpResponse {
        redirectNodeAddresses.forEach { node ->
            val response = client.get<HttpResponse>(String.format(path, node.address, nftId, cid))

            if (response.status == HttpStatusCode.OK) {
                println(response.headers)
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

    private fun parseCid(nftPath: NftPath): String {
        val path = nftPath.url!!.split("/")

        if (path.size != 5 || path[3].trim().isEmpty()) {
            throw IllegalArgumentException("Invalid nft path url")
        }

        return path[3]
    }

    private fun getNode() = config.trustedNodes[abs(nodeFlag.get()) % config.trustedNodes.size]

    private fun predicateRetry(message: String): (Exception) -> Boolean = {
        logger.warn("{}. Exception message: {}", message, it.message)
        if (it is NftException) {
            nodeFlag.incrementAndGet()
            true
        } else {
            false
        }
    }
}