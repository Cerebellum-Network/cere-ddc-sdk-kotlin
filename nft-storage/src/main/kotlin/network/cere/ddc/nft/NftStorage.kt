package network.cere.ddc.nft

import kotlinx.coroutines.runBlocking
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata

class NftStorage(private val client: TransportClient) {

    fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath =
        runBlocking { client.storeAsset(nftId, data, name) }

    fun readAsset(nftId: String, nftPath: NftPath): ByteArray =
        runBlocking { client.readAsset(nftId, nftPath) }

    fun storeMetadata(nftId: String, metadata: Metadata): NftPath =
        runBlocking { client.storeMetadata(nftId, metadata) }

    fun <T : Metadata> readMetadata(nftId: String, nftPath: NftPath, schema: Class<T>): T =
        runBlocking { client.readMetadata(nftId, nftPath, schema) }
}