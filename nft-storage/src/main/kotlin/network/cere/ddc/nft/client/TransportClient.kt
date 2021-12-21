package network.cere.ddc.nft.client

import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata

interface TransportClient : AutoCloseable {

    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath
    suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray
    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath
    suspend fun <T : Metadata> readMetadata(nftId: String, nftPath: NftPath, schema: Class<T>): T

}