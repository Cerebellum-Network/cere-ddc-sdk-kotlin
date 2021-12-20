package network.cere.ddc.nft.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.http.HttpSecurity
import network.cere.ddc.core.http.defaultHttpClient
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.Config
import network.cere.ddc.nft.exception.*
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class HttpTransportClient(
    private val scheme: Scheme,
    private val config: Config,
    httpClient: HttpClient = defaultHttpClient(),
) : TransportClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val CONTENT_PATH_HEADER = "Content-Path"
        const val CONTENT_SHA_256_HEADER = "Content-SHA256"
        const val CONTENT_SIGNATURE_HEADER = "Content-Signature"
        const val NFT_STANDART_HEADER = "Nft-Standard"
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

            install(JsonFeature)

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
            return storeData("$BASIC_NFT_URL/assets", nftId, data) {
                headers {
                    set(CONTENT_PATH_HEADER, name)
                }
            }
        } catch (e: Exception) {
            throw AssetSaveNftException("Couldn't store asset", e)
        }

    }

    override suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray {
        try {
            return readData("$BASIC_NFT_URL/assets/%s", nftId, nftPath)
        } catch (e: Exception) {
            throw AssetReadNftException("Couldn't read asset", e)
        }
    }

    override suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath {
        try {
            @Suppress("BlockingMethodInNonBlockingContext")
            val data = objectMapper.writeValueAsBytes(metadata)

            return storeData("$BASIC_NFT_URL/metadata", nftId, data) {
                headers {
                    set(NFT_STANDART_HEADER, metadata.schema)
                }
            }
        } catch (e: Exception) {
            throw MetadataSaveNftException("Couldn't store metadata", e)
        }

    }

    override suspend fun <T : Metadata> readMetadata(nftId: String, nftPath: NftPath, schema: Class<T>): T {
        try {
            val bytes = readData("$BASIC_NFT_URL/metadata/%s", nftId, nftPath)

            @Suppress("BlockingMethodInNonBlockingContext")
            return objectMapper.readValue(bytes, schema)
        } catch (e: Exception) {
            throw MetadataReadNftException("Couldn't read metadata", e)
        }
    }

    override fun close() {
        client.close()
    }

    private suspend fun readData(path: String, nftId: String, nftPath: NftPath): ByteArray {
        val node = nextNode()
        val cid = parseCid(nftPath)
        val response = client.get<HttpResponse>(String.format(path, node.address, nftId, cid))

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.readBytes()
            }
            HttpStatusCode.MultipleChoices -> {
                val redirectNodes: List<Node> = response.receive()
                redirectNodes.forEach { nodeAddresses[it.address] = it.id }
                redirectToNodes(nftId, redirectNodes)
            }
            else -> {
                throw NftException("Invalid response status for node=$node. Response: status=${response.status} body='${response.receive<String>()}'")
            }
        }
    }

    private suspend inline fun storeData(
        path: String,
        nftId: String,
        data: ByteArray,
        block: HttpRequestBuilder.() -> Unit
    ): NftPath {
        val hash = data.sha256()
        val node = nextNode()
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
            throw NftException("Invalid response status for node=$node. Response: status='${response.status}' body='${response.receive<String>()}'")
        }

        return response.receive()
    }

    private suspend fun redirectToNodes(nftId: String, redirectNodeAddresses: List<Node>): ByteArray {
        redirectNodeAddresses.forEach { node ->
            val response = client.get<HttpResponse>(String.format("$BASIC_NFT_URL/assets/%s", node.address, nftId))

            if (response.status == HttpStatusCode.OK) {
                return response.readBytes()
            }

            logger.warn("Couldn't get data from redirected node=$node. Response: status='${response.status}' body='${response.receive<String>()}'")
        }

        throw NftException("Couldn't get asset from nodes: $redirectNodeAddresses")
    }

    private fun parseCid(nftPath: NftPath): String {
        val path = nftPath.url.split("/")

        if (path.size != 5 || path[3].trim().isEmpty()) {
            throw IllegalArgumentException("Invalid nft path url")
        }

        return path[3]
    }

    private fun nextNode() = config.trustedNodes[nodeFlag.getAndIncrement() % config.trustedNodes.size]
}