package integration

import network.cere.ddc.core.extension.sha256
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.Config
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.client.HttpTransportClient
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

class AssetCommonTest {

    private val nftId = "AssetNftId"
    private val privateKey = "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60"

    private val scheme = Scheme.create(Scheme.ED_25519, privateKey)
    private val config = Config(listOf(Node(address = "http://localhost:8180", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT")))
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

        //when

        //then

    }
}