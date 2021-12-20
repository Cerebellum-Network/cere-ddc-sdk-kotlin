package integration

import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.Config
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.NftStorageAsync
import network.cere.ddc.nft.client.HttpTransportClient
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class MetadataCommonTest {

    private val nftId = "MetadataNftId"
    private val privateKey = "fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19"

    private val scheme = Scheme.create(Scheme.ETHEREUM, privateKey)
    private val config = Config(
        listOf(Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"))
    )
    private val client = HttpTransportClient(scheme, config)
    private val testSubject = NftStorage(client)

    @Test
    fun `Store metadata`() {
        //given
        val metadata =
            Erc721Metadata(name = "testName", description = "testDescription", image = "http://image-site.com")

        //when
        val result = testSubject.storeMetadata(nftId, metadata)

        //then
        assertThat(result.url).matches("cns:///.*/metadata.json")
    }

    @Test
    fun `Read metadata`() {
        //given
        val metadata = Erc1155Metadata(
            name = "testName",
            description = "testDescription",
            image = "http://image-site.com",
            decimals = 10
        )
        val nftPath = testSubject.storeMetadata(nftId, metadata)

        //when
        val result = testSubject.readMetadata(nftId, nftPath, Erc1155Metadata::class.java)

        //then
        assertThat(result).isEqualTo(metadata)
    }

    @Test
    fun `Read metadata redirect`() {
        //given
        val nodes = listOf(
            Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
            Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
            Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
            Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
        )
        val testSubject = NftStorageAsync(HttpTransportClient(scheme, Config(nodes)))

        val metadata = Erc1155Metadata(
            name = "testName",
            description = "testDescription",
            image = "http://image-site.com",
            decimals = 10
        )
        val nftPath = testSubject.storeMetadata(nftId, metadata).join()

        //wait until routing table update state for new asset
        Thread.sleep(3000)

        //when
        val resultDirectly = nodes.map {
            val client = NftStorageAsync(HttpTransportClient(scheme, Config(listOf(it))))
            client.readMetadata(nftId, nftPath, Erc1155Metadata::class.java)
        }

        //then
        resultDirectly.forEach {
            assertThat(it.join()).isEqualTo(metadata)
        }
    }
}