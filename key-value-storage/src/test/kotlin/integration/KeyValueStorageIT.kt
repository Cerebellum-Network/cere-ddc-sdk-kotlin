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

    private val privateKey =
        "0x500c89905aba00fc5211a2876b6105001ad8c77218b37b649ee38b4996e0716d6d5c7e03bbedbf39ca3588b5e6e0768e70a4507cd9e089f625929f9efae051f2"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val gatewayNodeUrl = "http://localhost:8090"
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
