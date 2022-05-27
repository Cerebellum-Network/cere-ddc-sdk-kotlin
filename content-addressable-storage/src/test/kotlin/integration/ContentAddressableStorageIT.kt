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

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val gatewayNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
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
