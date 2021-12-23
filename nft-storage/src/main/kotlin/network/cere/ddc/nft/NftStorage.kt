package network.cere.ddc.nft

import com.fasterxml.jackson.databind.JsonNode
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata

class NftStorage(private val client: TransportClient) {

    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath =
        client.storeAsset(nftId, data, name)

    suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray =
        client.readAsset(nftId, nftPath)

    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath =
        client.storeMetadata(nftId, metadata)

    suspend fun readMetadata(nftId: String, nftPath: NftPath): JsonNode =
        client.readMetadata(nftId, nftPath)
}