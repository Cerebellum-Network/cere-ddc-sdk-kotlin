package integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import network.cere.ddc.`object`.ObjectStorage
import network.cere.ddc.`object`.client.HttpTransportClient
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import org.junit.jupiter.api.Test
import java.time.Duration

internal class ObjectCommonTest {

    private val bucketId = "BucketId"
    private val privateKey = "9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60"

    private val scheme = Scheme.create(Scheme.ED_25519, privateKey)
    private val trustedNodes = listOf(
        Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT")
    )
    private val client = HttpTransportClient(scheme, trustedNodes)
    private val testSubject = ObjectStorage(client)

    @Test
    fun `Store object`() {
        runBlocking {
            //given
            val data = "Data for storing".toByteArray()

            //when
            val result = testSubject.storeObject(bucketId, data)

            //then
            result.url!! matches "cns://$bucketId/.*".toRegex()
        }
    }

    @Test
    fun `Read object`() {
        runBlocking {
            //given
            val data = "Data for reading".toByteArray()
            val objectPath = testSubject.storeObject(bucketId, data)

            //when
            val result = testSubject.readObject(bucketId, objectPath)

            //then
            result shouldBe data
        }
    }

    @Test
    fun `Read object redirect`() {
        runBlocking {
            //given
            val nodes = listOf(
                Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
                Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
                Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
                Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
            )
            val data = "Data for storing with redirect".toByteArray()
            val objectPath = testSubject.storeObject(bucketId, data)

            //wait until routing table update state for new data
            delay(Duration.ofSeconds(3))

            //when
            val resultDirectly = nodes.map {
                val client = ObjectStorage(HttpTransportClient(scheme, listOf(it)))
                client.readObject(bucketId, objectPath)
            }

            //then
            resultDirectly.forEach {
                it shouldBe data
            }
        }
    }
}