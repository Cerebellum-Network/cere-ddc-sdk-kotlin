package network.cere.ddc.`object`.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.`object`.config.TransportClientConfig
import network.cere.ddc.`object`.exception.ObjectException
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.core.extension.retry
import network.cere.ddc.core.http.HttpSecurity
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs

class HttpTransportClient(
    private val scheme: Scheme,
    private val trustedNodes: List<Node>,
    private val config: TransportClientConfig = TransportClientConfig(),
    httpClient: HttpClient = HttpClient(),
) : TransportClient {

    private val logger = LoggerFactory.getLogger(javaClass)

    private companion object {
        const val BUCKET_ID_HEADER = "Bucket-Id"
        const val BASIC_OBJECT_URL = "%s/api/rest/objects"
    }

    private val client: HttpClient
    private val nodeFlag = AtomicInteger()
    private val nodeAddresses = java.util.concurrent.ConcurrentHashMap(trustedNodes.associate { it.address to it.id })

    init {
        if (trustedNodes.isEmpty()) {
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

    override suspend fun storeObject(bucketId: String, data: ByteArray): ObjectPath =
        storeData(BASIC_OBJECT_URL, bucketId, data)

    override suspend fun readObject(bucketId: String, cid: String): ByteArray =
        readData("$BASIC_OBJECT_URL/$cid", bucketId)

    override suspend fun storeEdek(bucketId: String, cid: String, edek: Edek): Edek =
        storeData("$BASIC_OBJECT_URL/$cid/edek", bucketId, edek.copy(objectCid = cid)) {
            contentType(ContentType.Application.Json)
        }

    override suspend fun readEdek(bucketId: String, cid: String, publicKeyHex: String): Edek =
        readData("$BASIC_OBJECT_URL/$cid/edek/$publicKeyHex", bucketId)

    override fun close() {
        client.close()
    }

    private suspend inline fun <reified T> storeData(
        path: String, bucketId: String, data: Any, block: HttpRequestBuilder.() -> Unit = {}
    ): T = try {
        val node = getNode()
        retry(
            times = config.retryTimes, backOff = config.retryBackOff, { it is ObjectException || it is IOException }
        ) {
            val response = client.request<HttpResponse>(String.format(path, node.address)) {
                method = HttpMethod.Put
                body = data
                addBucketId(bucketId)
                block()
            }

            if (HttpStatusCode.Created != response.status) {
                throw ObjectException(
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
        path: String, bucketId: String, validation: (HttpResponse) -> (Unit) = {}
    ): T = try {
        val node = getNode()
        retry(
            times = config.retryTimes, backOff = config.retryBackOff, { it is ObjectException || it is IOException }
        ) {
            val response = client.get<HttpResponse>(String.format(path, node.address)) { addBucketId(bucketId) }

            return when (response.status) {
                HttpStatusCode.OK -> {
                    response
                }
                HttpStatusCode.MultipleChoices -> {
                    response.receive<List<Node>>()
                        .onEach { nodeAddresses[it.address] = it.id }
                        .let { redirectToNodes(path, bucketId, it) }
                }
                else -> {
                    throw ObjectException("Invalid response status for node=$node. Response: status=${response.status} body='${response.receive<String>()}'")
                }
            }.also(validation).receive()
        }
    } catch (e: Exception) {
        nodeFlag.incrementAndGet()
        throw e
    }

    private suspend inline fun <reified T> redirectToNodes(
        path: String, bucketId: String, redirectNodeAddresses: List<Node>
    ): T {
        redirectNodeAddresses.forEach { node ->
            val response = client.get<HttpResponse>(String.format(path, node.address)) { addBucketId(bucketId) }

            if (response.status == HttpStatusCode.OK) {
                return response.receive()
            }

            logger.warn(
                "Couldn't get data from redirected node={}. Response: status='{}' body='{}'",
                node,
                response.status,
                response.receive<String>()
            )
        }

        throw ObjectException("Couldn't get data from nodes: $redirectNodeAddresses")
    }

    private fun getNode() = trustedNodes[abs(nodeFlag.get()) % trustedNodes.size]

    private fun HttpRequestBuilder.addBucketId(bucketId: String) = headers { set(BUCKET_ID_HEADER, bucketId) }
}