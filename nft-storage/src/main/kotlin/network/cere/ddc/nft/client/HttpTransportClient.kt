package network.cere.ddc.nft.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.crypto.extension.sha256
import network.cere.ddc.crypto.model.Node
import network.cere.ddc.crypto.security.HttpSecurity
import network.cere.ddc.crypto.signature.Scheme
import network.cere.ddc.nft.exception.AssetReadNftException
import network.cere.ddc.nft.exception.AssetSaveNftException
import network.cere.ddc.nft.model.Metadata
import network.cere.ddc.nft.model.NftPath
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class HttpTransportClient(
    private val scheme: Scheme,
    private val trustedNodes: List<Node>,
    httpClient: HttpClient = HttpClient(Java) {
        install(HttpTimeout) {
            requestTimeoutMillis = Duration.ofMinutes(5).toMillis()
            connectTimeoutMillis = Duration.ofMinutes(3).toMillis()
        }
        engine { config { version(java.net.http.HttpClient.Version.HTTP_2) } }
    },
) : TransportClient {

    private companion object {
        const val CONTENT_PATH_HEADER = "Content-Path"
        const val CONTENT_SHA_256 = "Content-SHA256"
        const val CONTENT_SIGNATURE = "Content-Signature"
        const val BASIC_NFT_URL = "%s/api/rest/nfts/%s"
    }

    init {
        if (trustedNodes.isEmpty()) {
            throw IllegalStateException("Trusted node list is empty")
        }
    }

    private val nodeFlag = AtomicInteger()
    private val httpClient = httpClient.config {
        install(JsonFeature)

        install(HttpSecurity) {
            scheme = this@HttpTransportClient.scheme
            nodes = this@HttpTransportClient.trustedNodes
        }

        followRedirects = false
        expectSuccess = false
    }


    override suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath {
        val hash = data.sha256()
        val node = nextNode()
        val response =
            httpClient.request<HttpResponse>(
                String.format("$BASIC_NFT_URL/assets", node.address, nftId)
            ) {
                body = data
                method = HttpMethod.Put
                headers {
                    set(CONTENT_SHA_256, hash)
                    set(CONTENT_PATH_HEADER, name)
                    set(CONTENT_SIGNATURE, scheme.sign(hash.toByteArray()))
                }
            }

        if (HttpStatusCode.Created != response.status) {
            throw AssetSaveNftException("Couldn't save asset to ")
        }

        return response.receive()
    }

    override suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray {
        val node = nextNode()
        val response = httpClient.get<HttpResponse>(String.format("$BASIC_NFT_URL/assets/%s", node.address, nftId))

        return when (response.status) {
            HttpStatusCode.OK -> {
                response.receive()
            }
            HttpStatusCode.MultipleChoices -> {
                //ToDo parse as Node with Id, not only address
                redirectToNodes(nftId, response.receive())
                    ?: throw AssetReadNftException("Couldn't find asset from redirection")
            }
            else -> {
                throw AssetReadNftException("Invalid node response from node: $node. Response status: ${response.status}")
            }
        }
    }

    override suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath {
        TODO("Not yet implemented")
    }

    override suspend fun readMetadata(nftId: String, nftPath: NftPath): ByteArray {
        TODO("Not yet implemented")
    }

    private suspend fun redirectToNodes(nftId: String, redirectNodeAddresses: List<String>): ByteArray? {
        redirectNodeAddresses.forEach { address ->
            val response = httpClient.get<HttpResponse>(String.format("$BASIC_NFT_URL/assets/%s", address, nftId))

            if (response.status == HttpStatusCode.OK) {
                return response.receive()
            }

            //TODO debug logging for unsuccessful nodes
        }

        throw AssetReadNftException("Couldn't get asset from nodes: $redirectNodeAddresses")
    }


    private fun parseCid(nftPath: NftPath) {
        nftPath.url
    }

    private fun nextNode() = trustedNodes[nodeFlag.getAndIncrement() % trustedNodes.size]
}