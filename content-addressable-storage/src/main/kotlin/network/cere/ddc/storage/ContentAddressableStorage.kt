package network.cere.ddc.storage

import com.google.protobuf.ByteString
import io.ipfs.multibase.Base58
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.encryption.Cipher
import network.cere.ddc.core.encryption.EncryptedData
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.encryption.NaclCipher
import network.cere.ddc.core.extension.*
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.core.uri.Protocol
import network.cere.ddc.storage.config.ClientConfig
import network.cere.ddc.storage.domain.CreateSessionParams
import network.cere.ddc.storage.domain.Link
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.SearchResult
import network.cere.ddc.storage.domain.SearchType
import network.cere.ddc.storage.domain.StoreRequest
import network.cere.ddc.storage.domain.Tag
import org.komputing.khex.extensions.*
import pb.PieceOuterClass
import pb.QueryOuterClass
import pb.RequestOuterClass
import pb.ResponseOuterClass
import pb.SearchResultOuterClass
import pb.SessionStatusOuterClass
import pb.SignatureOuterClass
import pb.SignedPieceOuterClass
import pb.TagOuterClass
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InvalidObjectException
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


class ContentAddressableStorage(
    private val scheme: Scheme,
    private val cdnNodeUrl: String,
    private val clientConfig: ClientConfig = ClientConfig(),
    private val cidBuilder: CidBuilder = CidBuilder(),
    val cipher: Cipher = NaclCipher()
) {
    companion object {
        const val BASE_PATH_PIECES = "/api/v1/rest/pieces"
        const val BASE_PATH_SESSION = "/api/v1/rest/session"
        const val DEK_PATH_TAG = "dekPath"
        const val NONCE_TAG = "Nonce"
        const val READ_PARSE_ERROR_MESSAGE = "Couldn't parse read response body to SignedPiece."
        const val SEARCH_PARSE_ERROR_MESSAGE = "Couldn't parse search response body to SearchResult."
    }

    private val client: HttpClient = HttpClient().config {
        install(HttpTimeout) {
            requestTimeoutMillis = clientConfig.requestTimeout.toMillis()
            connectTimeoutMillis = clientConfig.connectTimeout.toMillis()
        }
        followRedirects = false
        expectSuccess = false
    }


    suspend fun store(bucketId: Long, piece: Piece): DdcUri {
        val request = buildStoreRequest(bucketId, piece)

        val response = sendRequest(BASE_PATH_PIECES) {
            method = HttpMethod.parse(request.method)
            body = request.body
        }
        val responseData = response.content
        val protoResponse = ResponseOuterClass.Response.parseFrom(responseData.toByteArray())
        if (!response.status.isSuccess()) {
            throw Exception("Failed to store. Response: status=${protoResponse.responseCode}, body=${protoResponse.body.toStringUtf8()}")
        }
        return DdcUri.Builder().protocol(Protocol.IPIECE).bucketId(bucketId).cid(request.cid).build()
    }

    private fun buildStoreRequest(bucketId: Long, piece: Piece): StoreRequest {
        val pbPiece: PieceOuterClass.Piece = piece.toProto(bucketId)
        val cid = cidBuilder.build(pbPiece.toByteArray())
        val timestamp = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC)
        val stringTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        val signatureString = "<Bytes>DDC store $cid at $stringTimestamp</Bytes>"
        val signature = scheme.sign(signatureString.toByteArray())
        val pbSignature = SignatureOuterClass.Signature
            .newBuilder()
            .setValue(ByteString.copyFromUtf8(signature))
            .setScheme(scheme.name)
            .setSigner(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
            .setTimestamp(timestamp.toInstant().toEpochMilli())
            .build()
        val pbSignedPiece: SignedPieceOuterClass.SignedPiece = SignedPieceOuterClass.SignedPiece
            .newBuilder()
            .setPiece(pbPiece.toByteString())
            .setSignature(pbSignature)
            .build()

        val outerRequest = RequestOuterClass.Request.newBuilder().setBody(ByteString.copyFrom(pbSignedPiece.toByteArray())).build()

        val requestSignature = signRequest(outerRequest, BASE_PATH_PIECES, HttpMethod.Put)

        val request = RequestOuterClass.Request.newBuilder()
            .setBody(pbSignedPiece.toByteString())
            .setScheme(scheme.name)
            .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
            .setSignature(ByteString.copyFrom(requestSignature.hexToBytes()))
            .build()

        return StoreRequest(request.toByteArray(), cid, HttpMethod.Put.value, BASE_PATH_PIECES)
    }

    private fun signRequest(request: RequestOuterClass.Request, path: String, method: HttpMethod = HttpMethod.Get): String {
        val cid = cidBuilder.build(
            concatArrays(
                getPath(path, method),
                specialIntToBytes(request.body.size()),
                request.body.toByteArray(),
                specialIntToBytes(request.sessionId.size()),
                request.sessionId.toByteArray(),
            ),
        )
        return scheme.sign("<Bytes>${cid}</Bytes>".toByteArray())
    }

    private fun concatArrays(vararg arrays: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        arrays.forEach { out.write(it) }
        return out.toByteArray()
    }

    private fun getPath(path: String, method: HttpMethod = HttpMethod.Get): ByteArray {
        val url = URL("${cdnNodeUrl}${path}")
        val query = if (url.query == null) "" else "?" + url.query.split('&').filter { it.split('=')[0] != "data" }.reduce { str1, str2 -> str1 + str2 }
        val link = "${url.path}${query}"
        return concatArrays(
            specialIntToBytes(method.value.length),
            method.value.toByteArray(),
            specialIntToBytes(link.length),
            link.toByteArray(),
        )
    }

    fun specialIntToBytes(i: Int): ByteArray {
        var secondByte: Byte = 0
        var i1 = i
        if (i1 >= 128) {
            secondByte++
        }
        while (i1 >= 256) {
            secondByte++
            i1 -= 128
        }
        return if (secondByte.toInt() == 0) {
            byteArrayOf(i1.toUByte().toByte())
        } else {
            byteArrayOf(i1.toUByte().toByte(), secondByte)
        }
    }

    suspend fun storeEncrypted(bucketId: Long, piece: Piece, encryptionOptions: EncryptionOptions): DdcUri {
        val encryptedData = cipher.encrypt(piece.data, encryptionOptions.dek)
        val newTags = mutableListOf(Tag(DEK_PATH_TAG, encryptionOptions.dekPath), Tag(NONCE_TAG, encryptedData.nonce.toHexString())) + piece.tags
        return this.store(bucketId, piece.copy(data = encryptedData.data, tags = newTags))
    }

    suspend fun readDecrypted(bucketId: Long, cid: String, dek: ByteArray): Piece {
        val piece = read(bucketId, cid)
        val nonce = piece.tags.first { it.key == NONCE_TAG }.value.hexToBytes()
        val decryptedData = cipher.decrypt(EncryptedData(piece.data, nonce), dek)
        return piece.copy(data = decryptedData)
    }

    suspend fun read(bucketId: Long, cid: String, session: ByteArray? = null): Piece {
        return retry(
            clientConfig.retryTimes,
            clientConfig.retryBackOff,
            { it is IOException && it !is InvalidObjectException }) {
            val request = if (session == null) {
                val requestSignature = signRequest(RequestOuterClass.Request.newBuilder().build(), "${BASE_PATH_PIECES}/${cid}?bucketId=${bucketId}")
                RequestOuterClass.Request.newBuilder()
                    .setScheme(scheme.name)
                    .setSignature(ByteString.copyFrom(requestSignature.hexToBytes()))
                    .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
                    .build()
            } else {
                RequestOuterClass.Request.newBuilder()
                    .setScheme(scheme.name)
                    .setSessionId(ByteString.copyFrom(session))
                    .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
                    .build()
            }
            val response = sendRequest(BASE_PATH_PIECES, cid) {
                method = HttpMethod.Get
                parameter("bucketId", bucketId)
                parameter("data", Base64.getEncoder().encodeToString(request.toByteArray()))
            }

            if (!response.status.isSuccess()) {
                throw RuntimeException(
                    "Failed to read. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }

            val pbResponse = runCatching { ResponseOuterClass.Response.parseFrom(response.receive<ByteArray>()) }
                .getOrElse { throw InvalidObjectException(READ_PARSE_ERROR_MESSAGE) }

            val pbSignedPiece = runCatching { SignedPieceOuterClass.SignedPiece.parseFrom(pbResponse.body) }
                .getOrElse { throw InvalidObjectException(READ_PARSE_ERROR_MESSAGE) }

            return parsePiece(PieceOuterClass.Piece.parseFrom(pbSignedPiece.piece))
        }
    }


    suspend fun search(query: Query, session: ByteArray? = null): SearchResult {
        val pbQuery = QueryOuterClass.Query.newBuilder()
            .setBucketId(query.bucketId.toInt())
            .addAllTags(query.tags.map {
                TagOuterClass.Tag.newBuilder()
                    .setKey(ByteString.copyFromUtf8(it.key))
                    .setValue(ByteString.copyFromUtf8(it.value))
                    .setSearchable(TagOuterClass.SearchType.valueOf(it.searchType.name))
                    .build()
            })
            .setSkipData(query.skipData)
            .build()
        val encodedQuery = Base58.encode(pbQuery.toByteArray())
        val request = if (session == null) {
            val requestSignature = signRequest(RequestOuterClass.Request.newBuilder().build(), "${BASE_PATH_PIECES}?query=${encodedQuery}")
            RequestOuterClass.Request.newBuilder()
                .setScheme(scheme.name)
                .setSignature(ByteString.copyFrom(requestSignature.hexToBytes()))
                .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
                .build()
        } else {
            RequestOuterClass.Request.newBuilder()
                .setScheme(scheme.name)
                .setSessionId(ByteString.copyFrom(session))
                .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
                .build()
        }
        return retry(
            clientConfig.retryTimes,
            clientConfig.retryBackOff,
            { it is IOException && it !is InvalidObjectException }) {
            val response = sendRequest(BASE_PATH_PIECES) {
                method = HttpMethod.Get
                parameter("query", encodedQuery)
                parameter("data", Base64.getEncoder().encodeToString(request.toByteArray()))
            }

            if (!response.status.isSuccess()) {
                throw RuntimeException(
                    "Failed to search. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }

            val pbResponse = runCatching { ResponseOuterClass.Response.parseFrom(response.receive<ByteArray>()) }
                .getOrElse { throw InvalidObjectException(SEARCH_PARSE_ERROR_MESSAGE) }

            val pbSearchResult = runCatching { SearchResultOuterClass.SearchResult.parseFrom(pbResponse.body) }
                .getOrElse { throw InvalidObjectException(SEARCH_PARSE_ERROR_MESSAGE) }

            return SearchResult(
                pieces = pbSearchResult.searchedPiecesList.map { sp -> parsePiece(PieceOuterClass.Piece.parseFrom(sp.signedPiece.piece), sp.cid) }
            )
        }
    }

    private fun parsePiece(pbPiece: PieceOuterClass.Piece, cid: String? = null) = Piece(
        data = pbPiece.data.toByteArray() ?: byteArrayOf(),
        tags = pbPiece.tagsList.map {
            Tag(it.key.toStringUtf8(), it.value.toStringUtf8(), SearchType.valueOf(it.searchable.name))
        },
        links = pbPiece.linksList.map { Link(it.cid, it.size, it.name) },
        cid = cid
    )

    private suspend fun sendRequest(apiUrl: String, path: String = "", block: HttpRequestBuilder.() -> Unit = {}): HttpResponse {
        val url = buildString {
            append(cdnNodeUrl)
            append(apiUrl)
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


    suspend fun createSession(createSessionParams: CreateSessionParams): ByteArray {
        val sessionId = UUID.randomUUID().toString().toByteArray().copyOf(21)
        val sessionStatus = SessionStatusOuterClass.SessionStatus.newBuilder()
            .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
            .setGas(createSessionParams.gas)
            .setSessionId(ByteString.copyFrom(sessionId))
            .setBucketId(createSessionParams.bucketId.toInt())
            .setEndOfEpoch(createSessionParams.endOfEpoch)
            .build()

        val signature = signRequest(
            RequestOuterClass.Request.newBuilder()
                .setBody(sessionStatus.toByteString())
                .setScheme(scheme.name)
                .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
                .build(),
            BASE_PATH_SESSION,
            HttpMethod.Post,
        )

        val request = RequestOuterClass.Request.newBuilder()
            .setBody(sessionStatus.toByteString())
            .setScheme(scheme.name)
            .setSignature(ByteString.copyFrom(signature.hexToBytes()))
            .setPublicKey(ByteString.copyFrom(scheme.publicKeyHex.hexToBytes()))
            .build()

        retry(
            clientConfig.retryTimes,
            clientConfig.retryBackOff,
            { it is IOException && it !is InvalidObjectException }) {

            val response = sendRequest(BASE_PATH_SESSION) {
                method = HttpMethod.Post
                body = request.toByteArray()
            }

            if (!response.status.isSuccess()) {
                throw RuntimeException(
                    "Failed to create session. Response: status='${response.status}' body=${response.receive<String>()}"
                )
            }
        }

        return sessionId
    }
}
