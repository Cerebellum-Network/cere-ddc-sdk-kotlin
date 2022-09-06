package network.cere.ddc.storage

import com.google.protobuf.ByteString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.encryption.Cipher
import network.cere.ddc.core.encryption.EncryptedData
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.encryption.NaclCipher
import network.cere.ddc.core.extension.hexToBytes
import network.cere.ddc.core.extension.retry
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.proto.Storage
import network.cere.ddc.storage.config.ClientConfig
import network.cere.ddc.storage.domain.Link
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.PieceUri
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.SearchResult
import network.cere.ddc.storage.domain.Tag
import org.komputing.khex.extensions.toHexString
import java.io.IOException
import java.io.InvalidObjectException


class ContentAddressableStorage(
    private val scheme: Scheme,
    private val cdnNodeUrl: String,
    private val clientConfig: ClientConfig = ClientConfig(),
    private val cidBuilder: CidBuilder = CidBuilder(),
    private val cipher: Cipher = NaclCipher()
) {
    private companion object {
        const val BASE_PATH = "/api/rest/pieces"
        const val DEK_PATH_TAG = "dekPath"
        const val NONCE_TAG = "Nonce"
    }

    private val client: HttpClient = HttpClient().config {
        install(HttpTimeout) {
            requestTimeoutMillis = clientConfig.requestTimeout.toMillis()
            connectTimeoutMillis = clientConfig.connectTimeout.toMillis()
        }

        followRedirects = false
        expectSuccess = false
    }

    suspend fun store(bucketId: Long, piece: Piece): PieceUri {
        val pbPiece = Storage.Piece.newBuilder()
            .setBucketId(bucketId)
            .setData(ByteString.copyFrom(piece.data))
            .addAllTags(piece.tags.map { Storage.Tag.newBuilder().setKey(it.key).setValue(it.value).build() })
            .addAllLinks(piece.links.map {
                Storage.Link.newBuilder().setCid(it.cid).setSize(it.size).setName(it.name).build()
            })
            .build()

        val pieceAsBytes = pbPiece.toByteArray()
        val cid = cidBuilder.build(pieceAsBytes)
        val signature = scheme.sign(cid.toByteArray())

        val signedPiece = Storage.SignedPiece.newBuilder()
            .setPiece(pbPiece)
            .setSignature(
                Storage.Signature.newBuilder()
                    .setValue(signature)
                    .setScheme(scheme.name)
                    .setSigner(scheme.publicKeyHex)
            )
            .build()

        return retry(clientConfig.retryTimes, clientConfig.retryBackOff, { it is IOException }) {
            val response = sendRequest {
                method = HttpMethod.Put
                body = signedPiece.toByteArray()
            }

            if (HttpStatusCode.Created != response.status) {
                throw RuntimeException(
                    "Failed to store. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }

            return PieceUri(bucketId, cid)
        }
    }

    suspend fun storeEncrypted(bucketId: Long, piece: Piece, encryptionOptions: EncryptionOptions): PieceUri {
        val encryptedData = cipher.encrypt(piece.data, encryptionOptions.dek)
        val newTags = mutableListOf(Tag(DEK_PATH_TAG, encryptionOptions.dekPath), Tag(NONCE_TAG, encryptedData.nonce.toHexString())) + piece.tags
        return this.store(bucketId, piece.copy(data = encryptedData.data, tags = newTags))
    }

    //TODO think about overload read method during writing DDC client
    suspend fun readDecrypted(bucketId: Long, cid: String, dek: ByteArray): Piece {
        val piece = read(bucketId, cid)
        val nonce = piece.tags.first { it.key == NONCE_TAG }.value.hexToBytes()
        val decryptedData = cipher.decrypt(EncryptedData(piece.data, nonce), dek)
        return piece.copy(data = decryptedData)
    }

    suspend fun read(bucketId: Long, cid: String): Piece {
        return retry(
            clientConfig.retryTimes,
            clientConfig.retryBackOff,
            { it is IOException && it !is InvalidObjectException }) {
            val response = sendRequest(cid) {
                method = HttpMethod.Get
                parameter("bucketId", bucketId)
            }

            if (HttpStatusCode.OK != response.status) {
                throw RuntimeException(
                    "Failed to read. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }

            val pbSignedPiece = runCatching { Storage.SignedPiece.parseFrom(response.receive<ByteArray>()) }
                .getOrElse { throw InvalidObjectException("Couldn't parse read response body to SignedPiece.") }

            return parsePiece(pbSignedPiece.piece)
        }
    }

    suspend fun search(query: Query): SearchResult {
        val pbQuery = Storage.Query.newBuilder()
            .setBucketId(query.bucketId)
            .addAllTags(query.tags.map { Storage.Tag.newBuilder().setKey(it.key).setValue(it.value).build() })
            .setSkipData(query.skipData)
            .build()

        return retry(
            clientConfig.retryTimes,
            clientConfig.retryBackOff,
            { it is IOException && it !is InvalidObjectException }) {
            val response = sendRequest {
                method = HttpMethod.Get
                body = pbQuery.toByteArray()
            }

            if (HttpStatusCode.OK != response.status) {
                throw RuntimeException(
                    "Failed to search. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }

            val pbSearchResult = runCatching { Storage.SearchResult.parseFrom(response.receive<ByteArray>()) }
                .getOrElse { throw InvalidObjectException("Couldn't parse search response body to SearchResult.") }

            return SearchResult(
                pieces = pbSearchResult.searchedPiecesList.map { sp -> parsePiece(sp.signedPiece.piece, sp.cid) }
            )
        }
    }

    private fun parsePiece(pbPiece: Storage.Piece, cid: String? = null) = Piece(
        data = pbPiece.data.toByteArray() ?: byteArrayOf(),
        tags = pbPiece.tagsList.map { Tag(it.key, it.value) },
        links = pbPiece.linksList.map { Link(it.cid, it.size, it.name) },
        cid = cid
    )

    private suspend fun sendRequest(path: String = "", block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        val url = buildString {
            append(cdnNodeUrl)
            append(BASE_PATH)
            path.takeIf(String::isNotEmpty)?.also { append("/").append(path) }
        }
        val request = HttpRequestBuilder().apply {
            url(url)
            block()
        }
        return runCatching { client.request<HttpResponse>(request) }
            .getOrElse {
                val error = it.message?.let { ", error='$it'" } ?: ""
                throw IOException("Couldn't send request url='$url', method='${request.method.value}$error")
            }
    }
}
