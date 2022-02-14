package network.cere.ddc.storage

import com.google.protobuf.ByteString
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import network.cere.ddc.storage.config.ClientConfig
import network.cere.ddc.storage.domain.*
import network.cere.ddc.core.extension.retry
import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.proto.Storage
import java.io.IOException

class ContentAddressableStorage(
    private val scheme: Scheme,
    private val gatewayNodeUrl: String,
    private val clientConfig: ClientConfig = ClientConfig(),
    private val cidBuilder: CidBuilder = CidBuilder(),
) {
    private companion object {
        const val BASE_PATH = "/api/rest/pieces"
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
            val response = client.request<HttpResponse>(gatewayNodeUrl + BASE_PATH) {
                method = HttpMethod.Put
                body = signedPiece.toByteArray()
            }

            if (HttpStatusCode.Created != response.status) {
                throw RuntimeException(
                    "Failed to store. Response: status='${response.status}' body='${response.receive<String>()}'"
                )
            }

            return PieceUri(bucketId, cid)
        }
    }

    suspend fun read(bucketId: Long, cid: String): Piece {
        return retry(clientConfig.retryTimes, clientConfig.retryBackOff, { it is IOException }) {
            val response = client.request<HttpResponse>("$gatewayNodeUrl$BASE_PATH/$cid") {
                method = HttpMethod.Get
            }

            if (HttpStatusCode.OK != response.status) {
                throw RuntimeException(
                    "Failed to read. Response: status='${response.status}' body='${response.receive<String>()}'"
                )
            }

            val pbSignedPiece = Storage.SignedPiece.parseFrom(response.receive<ByteArray>())

            return Piece(
                data = pbSignedPiece.piece.data.toByteArray(),
                tags = pbSignedPiece.piece.tagsList.map { Tag(it.key, it.value) }
            )
        }
    }

    suspend fun search(query: Query): SearchResult {
        val pbQuery = Storage.Query.newBuilder()
            .setBucketId(query.bucketId)
            .addAllTags(query.tags.map { Storage.Tag.newBuilder().setKey(it.key).setValue(it.value).build() })
            .build()

        return retry(clientConfig.retryTimes, clientConfig.retryBackOff, { it is IOException }) {
            val response = client.request<HttpResponse>(gatewayNodeUrl + BASE_PATH) {
                method = HttpMethod.Get
                body = pbQuery.toByteArray()
            }

            if (HttpStatusCode.OK != response.status) {
                throw RuntimeException(
                    "Failed to search. Response: status='${response.status}' body='${response.receive<String>()}'"
                )
            }

            val pbSearchResult = Storage.SearchResult.parseFrom(response.receive<ByteArray>())

            return SearchResult(
                pieces = pbSearchResult.signedPiecesList.map { sp ->
                    Piece(
                        data = sp.piece.data.toByteArray(),
                        tags = sp.piece.tagsList.map { Tag(it.key, it.value) }
                    )
                })
        }
    }
}