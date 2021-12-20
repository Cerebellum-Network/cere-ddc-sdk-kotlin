package network.cere.ddc.nft

import kotlinx.coroutines.runBlocking
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class NftStorageAsyncTest {

    private val client: TransportClient = mock {}

    private val testSubject = NftStorageAsync(client)

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
            Assertions.assertThat(result).isEqualTo(NftPath(url))
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
            Assertions.assertThat(result).isEqualTo(data)
            verify(client).readAsset(nftId, NftPath(url))
        }
    }
}