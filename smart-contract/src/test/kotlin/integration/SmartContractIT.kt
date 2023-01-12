package integration

import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.signature.Scheme
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

internal class SmartContractIT {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val cdnNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val timestampTomorrow = Instant.now().plus(1, DAYS)

    private val encryptionSecretPhrase = "style banner please love bring point zebra lounge match morning list prison"

    //    private val clientOptions = ClientOptions(cdnNodeUrl)
    private val balance = 1L
    private val resource = 1L
    private val clusterId = 2L

    @Test
    fun `test`() {
        runBlocking {
            //given


            //when

            //then

        }
    }
}
