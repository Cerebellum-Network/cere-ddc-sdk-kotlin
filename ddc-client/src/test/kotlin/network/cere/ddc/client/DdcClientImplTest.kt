package network.cere.ddc.client

import kotlinx.coroutines.runBlocking
import network.cere.ddc.contract.options.ClientOptions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class DdcClientImplTest {

    private val encryptionSecretPhrase = "style banner please love bring point zebra lounge match morning list prison"
    private val cdnNodeUrl = "http://localhost:8080"
    private val clientOptions = ClientOptions(cdnNodeUrl)
    private val balance = 1L
    private val resource = 1L
    private val clusterId = 2L

    @Test
    fun createBucket() {
        runBlocking {
            val ddcClient = DdcClientImpl.buildAndConnect(clientOptions, encryptionSecretPhrase, null)
            val bucket = ddcClient.createBucket(balance, resource, clusterId, null)
            assertNotNull(bucket)
        }

    }
}