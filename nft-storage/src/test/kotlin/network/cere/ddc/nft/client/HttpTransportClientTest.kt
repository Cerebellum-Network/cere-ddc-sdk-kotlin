package network.cere.ddc.nft.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.MimeType
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftStorageConfig
import network.cere.ddc.nft.model.NftPath
import org.junit.jupiter.api.*
import org.mockito.Mockito
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.times

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HttpTransportClientTest {

    private lateinit var server: WireMockServer

    private val nodeId = "123456789"
    private val nftId = "nftId"
    private val publicKey = "0xd75a980182b10ab7d54bfed3c964073a0ee172f3daa62325af021a68f707511a"
    private val schemeName = "Ed25519"

    private lateinit var scheme: Scheme
    private lateinit var testSubject: HttpTransportClient

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort()).also {
            it.start()
        }

    }

    @BeforeEach
    fun beforeEach() {
        scheme = mock {
            on { it.name } doAnswer { schemeName }
            on { it.publicKeyHex } doAnswer { publicKey }
            on { it.sign(org.mockito.kotlin.any()) } doAnswer { "signature" }
        }

        testSubject =
            HttpTransportClient(
                scheme,
                NftStorageConfig(listOf(Node(address = "http://localhost:${server.port()}", id = nodeId)))
            )
    }

    @AfterAll
    fun afterAll() {
        server.stop()
    }

    @Test
    fun `Store asset`() {
        runBlocking {
            //given
            val fileName = "shadow.jar"
            val data = "testStoreAsset"
            server.stubFor(
                put(urlEqualTo("/api/rest/nfts/$nftId/assets")).willReturn(
                    aResponse().withStatus(HttpStatusCode.Created.value)
                        .withHeader("Content-Type", MimeType.JSON.toString())
                        .withBody("""{ "url":"cns://routing-key/cid/$fileName" }""")
                )
            )

            //when
            val result = testSubject.storeAsset(nftId, data.toByteArray(), fileName)

            //then
            result shouldBe NftPath("cns://routing-key/cid/$fileName")
            server.verify(
                putRequestedFor(urlEqualTo("/api/rest/nfts/$nftId/assets"))
                    .withRequestBody(binaryEqualTo(data.toByteArray()))
                    .withHeader("Node-Id", equalTo(nodeId))
                    .withHeader("Content-SHA256", equalTo(data.toByteArray().sha256()))
                    .withHeader("Content-Signature", equalTo("signature"))
                    .withHeader("Request-Signature", equalTo("signature"))
                    .withHeader("Content-Path", equalTo(fileName))
                    .withHeader("Signing-Algorithm", equalTo(schemeName))
                    .withHeader("Client-Public-Key", equalTo(publicKey))
            )
            Mockito.verify(scheme, times(2)).sign(org.mockito.kotlin.any())
        }
    }

    @Test
    fun `Read asset`() {
        runBlocking {
            //given
            val data = "testStoreAsset".toByteArray()
            val path = NftPath("cns://routing-key/someCid/shadow.jar")

            server.stubFor(
                get(urlEqualTo("/api/rest/nfts/$nftId/assets/someCid")).willReturn(
                    aResponse().withStatus(HttpStatusCode.OK.value).withBody(data)
                )
            )

            //when
            val result = testSubject.readAsset(nftId, path)

            //then
            result shouldBe data
            server.verify(
                getRequestedFor(urlEqualTo("/api/rest/nfts/$nftId/assets/someCid"))
                    .withHeader("Node-Id", equalTo(nodeId))
                    .withHeader("Request-Signature", equalTo("signature"))
                    .withHeader("Signing-Algorithm", equalTo(schemeName))
                    .withHeader("Client-Public-Key", equalTo(publicKey))
            )
            Mockito.verify(scheme).sign(org.mockito.kotlin.any())
        }
    }

    @Test
    fun `Read asset redirect`() {
        runBlocking {
            //given
            val data = "testStoreAsset".toByteArray()
            val path = NftPath("cns://routing-key/someCid/shadow.jar")

            server.stubFor(
                get(urlEqualTo("/api/rest/nfts/$nftId/assets/someCid")).willReturn(
                    aResponse().withStatus(HttpStatusCode.OK.value).withBody(data)
                )
            )

            //when
            val result = testSubject.readAsset(nftId, path)

            //then
            result shouldBe data
            server.verify(
                getRequestedFor(urlEqualTo("/api/rest/nfts/$nftId/assets/someCid"))
                    .withHeader("Node-Id", equalTo(nodeId))
                    .withHeader("Request-Signature", equalTo("signature"))
                    .withHeader("Signing-Algorithm", equalTo(schemeName))
                    .withHeader("Client-Public-Key", equalTo(publicKey))
            )

            Mockito.verify(scheme).sign(org.mockito.kotlin.any())
        }
    }

}