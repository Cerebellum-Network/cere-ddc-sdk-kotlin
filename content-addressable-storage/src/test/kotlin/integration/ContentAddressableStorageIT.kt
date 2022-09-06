package integration

import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.Tag
import org.junit.jupiter.api.Test

internal class ContentAddressableStorageIT {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val cdnNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val testSubject = ContentAddressableStorage(scheme, cdnNodeUrl)

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

    @Test
    fun `Encrypted store and read`() {
        runBlocking {
            //given
            val bucketId = 1L
            val piece = Piece(
                data = "Hello world!".toByteArray(),
                tags = listOf(Tag(key = "Creator", value = "Jack"))
            )
            val encryptionOptions = EncryptionOptions("/some/path", "11111111111111111111111111111111".toByteArray())

            //when
            val pieceUrl = testSubject.storeEncrypted(bucketId, piece, encryptionOptions)
            val savedPiece = testSubject.readDecrypted(bucketId, pieceUrl.cid, encryptionOptions.dek)

            //then
            pieceUrl.toString() shouldBe "cns://$bucketId/${pieceUrl.cid}"
            savedPiece.data contentEquals  piece.data
            savedPiece.tags shouldContainAll listOf(Tag(key = "Creator", value = "Jack"), Tag(key = "dekPath", value = "/some/path"))
        }
    }

    @Test
    fun `Search with data`() {
        runBlocking {
            //given
            val bucketId = 1L
            val tags = listOf(Tag(key = "Search", value = "test"))
            val piece = Piece(data = "Hello world!".toByteArray(), tags = tags)
            val pieceUrl = testSubject.store(bucketId, piece)

            //when
            val result = testSubject.search(Query(bucketId, tags))

            //then
            result.pieces shouldContainExactly listOf(piece.copy(cid = pieceUrl.cid))
        }
    }

    @Test
    fun `Search without data`() {
        runBlocking {
            //given
            val bucketId = 1L
            val tags = listOf(Tag(key = "Search", value = "test"))
            val piece = Piece(data = "Hello world!".toByteArray(), tags = tags)
            val pieceUrl = testSubject.store(bucketId, piece)

            //when
            val result = testSubject.search(Query(bucketId, tags, skipData = true))

            //then
            result.pieces shouldContainExactly listOf(piece.copy(data = byteArrayOf(), cid = pieceUrl.cid))
        }
    }
}
