package integration

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.ContentAddressableStorage
import network.cere.ddc.storage.domain.CreateSessionParams
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.SearchType
import network.cere.ddc.storage.domain.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

internal class ContentAddressableStorageIT {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val cdnNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val testSubject = ContentAddressableStorage(scheme, cdnNodeUrl)
    private val timestampTomorrow = Instant.now().plus(1, DAYS)

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

            //then
            val savedPiece = testSubject.read(bucketId, pieceUrl.cid)

            savedPiece shouldBeEqualToComparingFields piece
        }
    }

    @Test
    fun `Store and read with session`() {
        runBlocking {
            //given
            val bucketId = 1L
            val piece = Piece(
                data = "Hello world!".toByteArray(),
                tags = listOf(Tag(key = "Creator", value = "Jack"))
            )

            //when
            val session = testSubject.createSession(CreateSessionParams(1000000, timestampTomorrow.toEpochMilli(), bucketId))
            val storeRequest = testSubject.store(bucketId, piece)

            //thenSearchable
            storeRequest.cid shouldNotBe null

            val readRequest = testSubject.read(bucketId, storeRequest.cid, session)
            readRequest.data shouldBe piece.data
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

    @Test
    fun `Search not searchable`() {
        runBlocking {
            //given
            val bucketId = 2L
            val tags = listOf(Tag(key = "Search2", value = "test2", SearchType.NOT_SEARCHABLE))

            //when
            val result = testSubject.search(Query(bucketId, tags))

            //then
            result.pieces.isEmpty() shouldBe true
        }
    }

    @Test
    fun `Store and read encrypted`() {
        runBlocking {
            //given
            val bucketId = 1L
            val piece = Piece(
                data = "Hello world!".toByteArray(),
                tags = listOf(Tag(key = "Creator", value = "Jack"))
            )
            val encryptionOptions = EncryptionOptions("/", "12343333333333333333333333333333".toByteArray())

            //when
            val pieceUrl = testSubject.storeEncrypted(bucketId, piece, encryptionOptions)

            //then
            val savedPiece = testSubject.readDecrypted(bucketId, pieceUrl.cid, encryptionOptions.dek)

            savedPiece.data shouldBe piece.data
        }
    }
}
