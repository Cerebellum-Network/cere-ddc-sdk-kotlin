package network.cere.ddc.storage

import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.config.ClientConfig
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.domain.PieceUri
import network.cere.ddc.storage.domain.Query
import network.cere.ddc.storage.domain.Tag

class KeyValueStorage(
    scheme: Scheme,
    cdnNodeUrl: String,
    clientConfig: ClientConfig = ClientConfig(),
    cidBuilder: CidBuilder = CidBuilder(),
) {

    companion object {
        const val keyTag = "Key"
    }

    private val caStorage = ContentAddressableStorage(scheme, cdnNodeUrl, clientConfig, cidBuilder)

    suspend fun store(bucketId: Long, key: String, piece: Piece): PieceUri {
        if (piece.tags.any { it.key == keyTag }) {
            throw RuntimeException("'Key' is a reserved tag for key-value storage")
        }

        return caStorage.store(
            bucketId, Piece(
                data = piece.data,
                tags = piece.tags.toMutableList().apply { this.add(Tag(key = keyTag, value = key)) }
            )
        )
    }

    suspend fun read(bucketId: Long, key: String): List<Piece> {
        return caStorage.search(Query(bucketId = bucketId, tags = listOf(Tag(key = keyTag, value = key))))
            .pieces.map { p -> Piece(data = p.data, p.tags.filter { it.key != keyTag }) }
    }
}
