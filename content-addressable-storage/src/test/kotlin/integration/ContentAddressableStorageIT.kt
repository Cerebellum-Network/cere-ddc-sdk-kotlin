package integration

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Tag
import org.junit.jupiter.api.Test

internal class ContentAddressableStorageIT {

    private val privateKey =
        "0x500c89905aba00fc5211a2876b6105001ad8c77218b37b649ee38b4996e0716d6d5c7e03bbedbf39ca3588b5e6e0768e70a4507cd9e089f625929f9efae051f2"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val gatewayNodeUrl = "http://localhost:8080"
    private val testSubject = ContentAddressableStorage(scheme, gatewayNodeUrl)

    @Test
    fun `Store and read`() {
        runBlocking {
            //given
            val bucketId = 1L
            val piece = Piece(
                data = "Hello world!".toByteArray(),
                tags = listOf(Tag(key = "Creator", value = "Jack"))
            )

            //when
            val pieceUrl = testSubject.store(bucketId, piece)
            val savedPiece = testSubject.read(bucketId, pieceUrl.cid)

            //then
            pieceUrl.toString() shouldBe "cns://$bucketId/${pieceUrl.cid}"
            savedPiece shouldBeEqualToComparingFields piece
        }
    }
}
