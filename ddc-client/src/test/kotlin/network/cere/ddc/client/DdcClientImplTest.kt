package network.cere.ddc.client

import kotlinx.coroutines.runBlocking
import network.cere.ddc.contract.options.ClientOptions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class DdcClientImplTest {

    private val privateKey = "0x4004b8619c607297a1747dcea04e937753094616d378ee941bc880494262f552"
    private val cdnNodeUrl = "http://localhost:8080"
    private val clientOptions = ClientOptions(cdnNodeUrl)
    private val balance = 1L
    private val resource = 1L
    private val clusterId = 2L

    @Test
    fun buildAndConnectClient() {
        runBlocking {
            val ddcClient = DdcClientImpl.buildAndConnect(clientOptions, privateKey, null)
            assertNotNull(ddcClient)
        }

    }
    @Test
    fun createBucket() {
        runBlocking {
            val ddcClient = DdcClientImpl.buildAndConnect(clientOptions, privateKey, null)
            val bucket = ddcClient.createBucket(balance, resource, clusterId, null)
            assertNotNull(bucket)
        }

    }
}