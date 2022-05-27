package integration

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.KeyValueStorage
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Tag
import org.junit.jupiter.api.Test

internal class KeyValueStorageIT {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val gatewayNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val testSubject = KeyValueStorage(scheme, gatewayNodeUrl)

    @Test
    fun `Store and read`() {
        runBlocking {
            //given
            val bucketId = 1L
            val key = "super nft"
            val piece = Piece(
                data = "Hello world!".toByteArray(),
                tags = listOf(Tag(key = "Creator", value = "Jack"))
            )

            //when
            testSubject.store(bucketId, key, piece)
            val savedPieces = testSubject.read(bucketId, key)

            //then
            savedPieces shouldHaveSize 1
            savedPieces.first() shouldBeEqualToComparingFields  piece
        }
    }
}
