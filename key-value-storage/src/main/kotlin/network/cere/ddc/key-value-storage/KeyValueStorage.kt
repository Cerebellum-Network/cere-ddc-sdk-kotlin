package network.cere.ddc.`key-value-storage`

import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.`ca-storage`.ContentAddressableStorage
import network.cere.ddc.`ca-storage`.config.ClientConfig
import network.cere.ddc.`ca-storage`.domain.Piece
import network.cere.ddc.`ca-storage`.domain.Query
import network.cere.ddc.`ca-storage`.domain.Tag

class KeyValueStorage{
    private val caStorage : ContentAddressableStorage
    constructor(
        caStorage : ContentAddressableStorage
    ) {
        this.caStorage = caStorage
    }
    constructor(
        scheme: Scheme,
        cdnNodeUrl: String,
        clientConfig: ClientConfig = ClientConfig(),
        cidBuilder: CidBuilder = CidBuilder(),
    ) {
        this.caStorage = ContentAddressableStorage(scheme, cdnNodeUrl, clientConfig, cidBuilder)
    }

    companion object {
        const val keyTag = "Key"
    }

    suspend fun store(bucketId: Long, key: String, piece: Piece): DdcUri {
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

    suspend fun read(bucketId: Long, key: String, session: ByteArray? = null): List<Piece> {
        return caStorage.search(Query(bucketId = bucketId, tags = listOf(Tag(key = keyTag, value = key))), session)
            .pieces.map { p -> Piece(data = p.data, p.tags.filter { it.key != keyTag }) }
    }
}
