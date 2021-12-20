package integration

import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.Config
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.NftStorageAsync
import network.cere.ddc.nft.client.HttpTransportClient
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

class AssetCommonTest {

    private val nftId = "AssetNftId"
    private val privateKey = "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60"

    private val scheme = Scheme.create(Scheme.ED_25519, privateKey)
    private val config = Config(
        listOf(
            Node(
                address = "http://localhost:8180",
                id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"
            )
        )
    )
    private val client = HttpTransportClient(scheme, config)
    private val testSubject = NftStorage(client)

    @Test
    fun `Store asset`() {
        //given
        val asset = "Asset for storing".toByteArray()
        val name = "someAsset.jpeg"

        //when
        val result = testSubject.storeAsset(nftId, asset, name)

        //then
        assertThat(result.url).matches("cns:///.*/$name")
    }

    @Test
    fun `Read asset`() {
        //given
        val asset = "Asset for reading".toByteArray()
        val nftPath = testSubject.storeAsset(nftId, asset, "someReadAsset.jpeg")

        //when
        val result = testSubject.readAsset(nftId, nftPath)

        //then
        assertThat(result).isEqualTo(asset)
    }

    @Test
    fun `Read asset redirect`() {
        //given
        val nodes = listOf(
            Node(address = "http://localhost:8180", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
            Node(address = "http://localhost:8181", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
            Node(address = "http://localhost:8182", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
            Node(address = "http://localhost:8183", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
        )
        val testSubject = NftStorageAsync(HttpTransportClient(scheme, Config(nodes)))

        val asset = "Asset for storing with redirect".toByteArray()
        val name = "someAsset.jpeg"
        val nftPath = testSubject.storeAsset(nftId, asset, name).join()

        //wait until routing table update state for new asset
        Thread.sleep(3000)

        //when
        val resultDirectly = nodes.map {
            val client = NftStorageAsync(HttpTransportClient(scheme, Config(listOf(it))))
            client.readAsset(nftId, nftPath)
        }

        //then
        resultDirectly.forEach {
            assertThat(it.join()).isEqualTo(asset)
        }
    }
}