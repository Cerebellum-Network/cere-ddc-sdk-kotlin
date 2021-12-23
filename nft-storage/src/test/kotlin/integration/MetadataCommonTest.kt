package integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftConnectionConfig
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.client.HttpTransportClient
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import org.junit.jupiter.api.Test
import java.time.Duration

class MetadataCommonTest {

    private val nftId = "MetadataNftId"
    private val privateKey = "fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val config = NftConnectionConfig(
        listOf(Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"))
    )
    private val client = HttpTransportClient(scheme, config)
    private val testSubject = NftStorage(client)

    @Test
    fun `Store metadata`() {
        runBlocking {
            //given
            val metadata =
                Erc721Metadata(name = "testName", description = "testDescription", image = "http://image-site.com")

            //when
            val result = testSubject.storeMetadata(nftId, metadata)

            //then
            result.url!! matches "cns:///.*/metadata.json".toRegex()
        }
    }

    @Test
    fun `Read metadata`() {
        runBlocking {
            //given
            val metadata = Erc1155Metadata(
                name = "testName",
                description = "testDescription",
                image = "http://image-site.com",
                decimals = 10
            )
            val nftPath = testSubject.storeMetadata(nftId, metadata)

            //when
            val result = testSubject.readMetadata(nftId, nftPath)

            //then
            result shouldBe metadata
        }
    }

    @Test
    fun `Read metadata redirect`() {
        runBlocking {
            //given
            val nodes = listOf(
                Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
                Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
                Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
                Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
            )
            val testSubject = NftStorage(HttpTransportClient(scheme, NftConnectionConfig(nodes)))

            val metadata = Erc1155Metadata(
                name = "testName",
                description = "testDescription",
                image = "http://image-site.com",
                decimals = 10
            )
            val nftPath = testSubject.storeMetadata(nftId, metadata)

            //wait until routing table update state for new asset
            delay(Duration.ofSeconds(3))

            //when
            val resultDirectly = nodes.map {
                val client = NftStorage(HttpTransportClient(scheme, NftConnectionConfig(listOf(it))))
                client.readMetadata(nftId, nftPath)
            }

            //then
            resultDirectly.forEach {
                it shouldBe metadata
            }
        }
    }
}