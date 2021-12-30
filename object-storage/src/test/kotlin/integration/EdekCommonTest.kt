package integration

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import network.cere.ddc.`object`.ObjectStorage
import network.cere.ddc.`object`.client.HttpTransportClient
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.core.model.Node
import network.cere.ddc.core.signature.Scheme
import org.junit.jupiter.api.Test
import java.time.Duration

internal class EdekCommonTest {

    private val metadata = "Metadata".toByteArray()
    private val privateKey = "fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val trustedNodes =
        listOf(Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"))
    private val client = HttpTransportClient(scheme, trustedNodes)
    private val testSubject = ObjectStorage(client)

    @Test
    fun `Store edek`() {
        runBlocking {
            //given
            val bucketId = 1L
            val objectPath = testSubject.storeObject(bucketId, metadata)
            val cid = objectPath.url!!.split("/")[3].trim()
            val edek = Edek(scheme.publicKeyHex, "1234567890", "wrongCid")

            //when
            val result = testSubject.storeEdek(objectPath, edek)

            //then
            result shouldBe edek.copy(objectCid = cid)
        }
    }

    @Test
    fun `Read edek`() {
        runBlocking {
            //given
            val bucketId = 2L
            val objectPath = testSubject.storeObject(bucketId, metadata)
            val edek = testSubject.storeEdek(objectPath, Edek(scheme.publicKeyHex, "1234567890"))

            //when
            val result = testSubject.readEdek(objectPath, scheme.publicKeyHex)

            //then
            result shouldBe edek
        }
    }

    @Test
    fun `Read edek redirect`() {
        runBlocking {
            //given
            val nodes = listOf(
                Node(address = "http://localhost:8080", id = "12D3KooWFRkkd4ycCPYEmeBzgfkrMrVSHWe6sYdgPo1JyAdLM4mT"),
                Node(address = "http://localhost:8081", id = "12D3KooWJ2h8af9sehgTKg6f2yPWYxLryVYbTAzpYQLLFp5GErxu"),
                Node(address = "http://localhost:8082", id = "12D3KooWPfi9EtgoZHFnHh1at85mdZJtj7L8n94g6LFk6e8EEk2b"),
                Node(address = "http://localhost:8083", id = "12D3KooWJLuJEmtYf3bakUwe2q1uMcnbCBKRg7GkpG6Ws74Aq6NC")
            )
            val bucketId = 3L
            val objectPath = testSubject.storeObject(bucketId, metadata)
            val edek = testSubject.storeEdek(objectPath, Edek(scheme.publicKeyHex, "1234567890"))


            //wait until routing table update state for new object
            delay(Duration.ofSeconds(3))

            //when
            val resultDirectly = nodes.map {
                val client = ObjectStorage(HttpTransportClient(scheme, listOf(it)))
                client.readEdek(objectPath, scheme.publicKeyHex)
            }

            //then
            resultDirectly.forEach {
                it shouldBe edek
            }
        }
    }
}