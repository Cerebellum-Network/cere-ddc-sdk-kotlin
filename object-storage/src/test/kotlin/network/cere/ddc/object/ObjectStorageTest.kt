package network.cere.ddc.`object`

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.`object`.client.TransportClient
import network.cere.ddc.`object`.model.ObjectPath
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ObjectStorageTest {

    private val bucketId = "testBucketId"

    private val client: TransportClient = mock {}

    private val testSubject = ObjectStorage(client)

    @Test
    fun `Store object`() {
        runBlocking {
            //given
            val data = "testData".toByteArray()
            val url = "cns://routing-key/cid"

            whenever(client.storeObject(bucketId, data)).thenReturn(ObjectPath(url))

            //when
            val result = testSubject.storeObject(bucketId, data)

            //then
            result shouldBe ObjectPath(url)
            verify(client).storeObject(bucketId, data)
        }
    }

    @Test
    fun `Read object`() {
        runBlocking {
            //given
            val data = "testData".toByteArray()
            val cid = "cid"
            val url = "cns://$bucketId/$cid"

            whenever(client.readObject(ObjectPath(url))).thenReturn(data)

            //when
            val result = testSubject.readObject(ObjectPath(url))

            //then
            result shouldBe data
            verify(client).readObject(ObjectPath(url))
        }
    }

}