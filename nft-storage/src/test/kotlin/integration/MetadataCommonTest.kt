package integration

import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.nft.Config
import network.cere.ddc.nft.NftStorage
import network.cere.ddc.nft.client.HttpTransportClient
import org.junit.jupiter.api.Test

class MetadataCommonTest {

    private val nftId = "MetadataNftId"
    private val privateKey = "fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19"

    private val scheme = Scheme.create(Scheme.ETHEREUM, privateKey)
    private val config = Config(listOf(Node(address = "http://localhost:8180", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT")))
    private val client = HttpTransportClient(scheme, config)
    private val testSubject = NftStorage(client)

    @Test
    fun `Store metadata`() {
        //given

        //when

        //then

    }

    @Test
    fun `Read metadata`() {
        //given

        //when

        //then

    }

    @Test
    fun `Read metadata redirect`() {
        //given

        //when

        //then

    }
}