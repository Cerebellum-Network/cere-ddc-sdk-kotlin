package network.cere.ddc.nft

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class NftStorageTest {

    private val nftId = "testNftId"

    private val client: TransportClient = mock {}

    private val testSubject = NftStorage(client)

    @Test
    fun `Store asset`() {
        runBlocking {
            //given
            val data = "testData".toByteArray()
            val fileName = "testFileName.mp4"
            val url = "cns://routing-key/cid/$fileName"

            whenever(client.storeAsset(nftId, data, fileName)).thenReturn(NftPath(url))

            //when
            val result = testSubject.storeAsset(nftId, data, fileName)

            //then
            result shouldBe NftPath(url)
            verify(client).storeAsset(nftId, data, fileName)
        }
    }

    @Test
    fun `Read asset`() {
        runBlocking {
            //given
            val data = "testData".toByteArray()
            val fileName = "testFileName.mp4"
            val url = "cns://routing-key/cid/$fileName"

            whenever(client.readAsset(nftId, NftPath(url))).thenReturn(data)

            //when
            val result = testSubject.readAsset(nftId, NftPath(url))

            //then
            result shouldBe data
            verify(client).readAsset(nftId, NftPath(url))
        }
    }

    @Test
    fun `Store metadata`() {
        runBlocking {
            //given
            val url = "cns://routing-key/cid/metadata.json"
            val metadata = Erc721Metadata(name = "someName", description = "someDescription", image = "someImage")
            whenever(client.storeMetadata(nftId, metadata)).thenReturn(NftPath(url))

            //when
            val result = testSubject.storeMetadata(nftId, metadata)

            //then
            result shouldBe NftPath(url)
            verify(client).storeMetadata(nftId, metadata)
        }
    }

    @Test
    fun `Read metadata`() {
        runBlocking {
            //given
            val metadata = jacksonObjectMapper().valueToTree<ObjectNode>(Erc721Metadata(name = "someName", description = "someDescription", image = "someImage"))
            val url = "cns://routing-key/cid/metadata.json"

            whenever(client.readMetadata(nftId, NftPath(url))).thenReturn(metadata)

            //when
            val result = testSubject.readMetadata(nftId, NftPath(url))

            //then
            result shouldBe metadata
            verify(client).readMetadata(nftId, NftPath(url))
        }
    }
}