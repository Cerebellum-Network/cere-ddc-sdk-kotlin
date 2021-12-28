package network.cere.ddc.`object`.client

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.times

internal class HttpTransportClientTest {

    private val nodeId = "123456789"
    private val bucketId = "bucketId"
    private val publicKey = "0xd75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a"
    private val schemeName = "Ed25519"

    private val scheme: Scheme = mock {
        on { it.name } doAnswer { schemeName }
        on { it.publicKeyHex } doAnswer { publicKey }
        on { it.sign(org.mockito.kotlin.any()) } doAnswer { "signature" }
    }

    @Test
    fun `Store object`() {
        runBlocking {
            //given
            val data = "testStoreData"
            var requestCaptor: HttpRequestData? = null
            val testSubject = createHttpTransportClient { request ->
                request.body.toByteArray()
                requestCaptor = request
                respond(
                    content = """{ "url":"cns://routing-key/cid" }""",
                    status = HttpStatusCode.Created,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            //when
            val result = testSubject.storeObject(bucketId, data.toByteArray())

            //then
            result shouldBe ObjectPath("cns://routing-key/cid")

            requestCaptor.assert("/api/rest/objects", data.toByteArray()).also {
                val headers = it.headers

                headers["Content-SHA256"] shouldBe data.toByteArray().sha256()
                headers["Content-Signature"] shouldBe "signature"
            }

            Mockito.verify(scheme, times(2)).sign(org.mockito.kotlin.any())
        }
    }

    @Test
    fun `Read object`() {
        runBlocking {
            //given
            val data = "testStoreData"
            var requestCaptor: HttpRequestData? = null
            val testSubject = createHttpTransportClient { request ->
                requestCaptor = request
                respondOk(content = data)
            }

            //when

            val result = testSubject.readObject(ObjectPath("cns://$bucketId/someCid"))

            //then
            result shouldBe data.toByteArray()
            requestCaptor.assert("/api/rest/objects/someCid", ByteArray(0))

            Mockito.verify(scheme).sign(org.mockito.kotlin.any())
        }
    }

    @Test
    fun `Read object redirect`() {
        runBlocking {
            //given
            val data = "testStoreData"
            val redirect = """[{"id":"$nodeId", "address":"http://localhost:8888"}]"""

            var requestCaptorRedirect: HttpRequestData? = null
            var requestCaptorContent: HttpRequestData? = null
            val testSubject = createHttpTransportClient { request ->
                if ("${request.url.protocol.name}://${request.url.host}:${request.url.port}" == "http://localhost:8080") {
                    requestCaptorRedirect = request
                    respond(
                        content = redirect,
                        status = HttpStatusCode.MultipleChoices,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                } else {
                    requestCaptorContent = request
                    respond(content = data, status = HttpStatusCode.OK)
                }
            }

            //when
            val result = testSubject.readObject(ObjectPath("cns://$bucketId/someCid"))

            //then
            result shouldBe data.toByteArray()
            requestCaptorRedirect.assert("/api/rest/objects/someCid", ByteArray(0)).also {
                "${it.url.protocol.name}://${it.url.host}:${it.url.port}" shouldBe "http://localhost:8080"
            }
            requestCaptorContent.assert("/api/rest/objects/someCid", ByteArray(0)).also {
                "${it.url.protocol.name}://${it.url.host}:${it.url.port}" shouldBe "http://localhost:8888"
            }

            Mockito.verify(scheme, times(2)).sign(org.mockito.kotlin.any())
        }
    }

    private fun createHttpTransportClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpTransportClient {
        return HttpTransportClient(
            scheme,
            listOf(Node(address = "http://localhost:8080", id = nodeId)),
            httpClient = HttpClient(MockEngine(handler))
        )
    }

    private suspend fun HttpRequestData?.assert(path: String, data: ByteArray) = this.shouldNotBeNull().also {
        it.url.encodedPath shouldBe path
        it.body.toByteArray() shouldBe data

        it.headers.assert()
    }

    private fun Headers.assert() {
        this["Node-Id"] shouldBe nodeId
        this["Request-Signature"] shouldBe "signature"
        this["Client-Public-Key"] shouldBe publicKey
        this["Signing-Algorithm"] shouldBe schemeName
        this["Bucket-Id"] shouldBe bucketId
    }
}