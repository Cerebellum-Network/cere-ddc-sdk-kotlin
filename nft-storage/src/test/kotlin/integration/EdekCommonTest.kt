package integration

import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.java.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.client.HttpTransportClient
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import org.junit.jupiter.api.Test
import java.time.Duration

internal class EdekCommonTest {

    private val nftId = "MetadataNftId"
    private val privateKey = "fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val trustedNodes =
        listOf(Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"))
    private val client = HttpTransportClient(scheme, trustedNodes, httpClient = HttpClient(Java))
    private val testSubject = NftStorage(client)

    @Test
    fun `Store edek`() {
        runBlocking {
            //given
            val nftPath = testSubject.storeMetadata(
                nftId,
                Erc721Metadata(
                    name = "EdekCommonTest1",
                    description = "testDescription",
                    image = "http://image-site.com"
                )
            )
            val cid = nftPath.url!!.split("/")[3].trim()
            val edek = Edek(scheme.publicKeyHex, "1234567890", "wrongCid")

            //when
            val result = testSubject.storeEdek(nftId, nftPath, edek)

            //then
            result shouldBe edek.copy(metadataCid = cid)
        }
    }

    @Test
    fun `Read edek`() {
        runBlocking {
            //given
            val nftPath = testSubject.storeMetadata(
                nftId,
                Erc721Metadata(
                    name = "EdekCommonTest2",
                    description = "testDescription",
                    image = "http://image-site.com"
                )
            )
            val edek = testSubject.storeEdek(nftId, nftPath, Edek(scheme.publicKeyHex, "1234567890"))

            //when
            val result = testSubject.readEdek(nftId, nftPath, scheme.publicKeyHex)

            //then
            result shouldBe edek
        }
    }

    @Test
    fun `Read edek redirect`() {
        runBlocking {
            //given
            val nodes = listOf(
                Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
                Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
                Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
                Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
            )
            val nftPath = testSubject.storeMetadata(
                nftId,
                Erc721Metadata(
                    name = "EdekCommonTest3",
                    description = "testDescription",
                    image = "http://image-site.com"
                )
            )
            val edek = testSubject.storeEdek(nftId, nftPath, Edek(scheme.publicKeyHex, "1234567890"))


            //wait until routing table update state for new asset
            delay(Duration.ofSeconds(3))

            //when
            val resultDirectly = nodes.map {
                val client = NftStorage(HttpTransportClient(scheme, listOf(it)))
                client.readEdek(nftId, nftPath, scheme.publicKeyHex)
            }

            //then
            resultDirectly.forEach {
                it shouldBe edek
            }
        }
    }
}