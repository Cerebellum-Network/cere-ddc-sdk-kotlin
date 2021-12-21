package network.cere.ddc.nft

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class NftStorageTest {

    private val client: TransportClient = mock {}

    private val testSubject = NftStorage(client)

    @Test
    fun `Store asset`() {
        runBlocking {
            //given
            val nftId = "testNftId"
            val data = "testData".toByteArray()
            val fileName = "testFileName.mp4"
            val url = "cns://routing-key/cid/$fileName"

            whenever(client.storeAsset(nftId, data, fileName)).thenReturn(NftPath(url))

            //when
            val result = testSubject.storeAsset(nftId, data, fileName).join()

            //then
            result shouldBe NftPath(url)
            verify(client).storeAsset(nftId, data, fileName)
        }
    }

    @Test
    fun `Read asset`() {
        runBlocking {
            //given
            val nftId = "testNftId"
            val data = "testData".toByteArray()
            val fileName = "testFileName.mp4"
            val url = "cns://routing-key/cid/$fileName"

            whenever(client.readAsset(nftId, NftPath(url))).thenReturn(data)

            //when
            val result = testSubject.readAsset(nftId, NftPath(url)).join()

            //then
            result shouldBe data
            verify(client).readAsset(nftId, NftPath(url))
        }
    }
}