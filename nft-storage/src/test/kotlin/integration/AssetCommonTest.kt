package integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.NftStorageConfig
import network.cere.ddc.nft.client.HttpTransportClient
import org.junit.jupiter.api.Test

class AssetCommonTest {

    private val nftId = "AssetNftId"
    private val privateKey = "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60"

    private val scheme = Scheme.create(Scheme.ED_25519, privateKey)
    private val config = NftStorageConfig(
        listOf(Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"))
    )
    private val client = HttpTransportClient(scheme, config)
    private val testSubject = NftStorage(client)

    @Test
    fun `Store asset`() {
        runBlocking {
            //given
            val asset = "Asset for storing".toByteArray()
            val name = "someAsset.jpeg"

            //when
            val result = testSubject.storeAsset(nftId, asset, name)

            //then
            result.url matches "cns:///.*/$name".toRegex()
        }
    }

    @Test
    fun `Read asset`() {
        runBlocking {
            //given
            val asset = "Asset for reading".toByteArray()
            val nftPath = testSubject.storeAsset(nftId, asset, "someReadAsset.jpeg")

            //when
            val result = testSubject.readAsset(nftId, nftPath)

            //then
            result shouldBe asset
        }
    }

    @Test
    fun `Read asset redirect`() {
        runBlocking {
            //given
            val nodes = listOf(
                Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
                Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
                Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
                Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
            )
            val testSubject = NftStorage(HttpTransportClient(scheme, NftStorageConfig(nodes)))

            val asset = "Asset for storing with redirect".toByteArray()
            val name = "someAsset.jpeg"
            val nftPath = testSubject.storeAsset(nftId, asset, name)

            //wait until routing table update state for new asset
            Thread.sleep(3000)

            //when
            val resultDirectly = nodes.map {
                val client = NftStorage(HttpTransportClient(scheme, NftStorageConfig(listOf(it))))
                client.readAsset(nftId, nftPath)
            }

            //then
            resultDirectly.forEach {
                it shouldBe asset
            }
        }
    }
}