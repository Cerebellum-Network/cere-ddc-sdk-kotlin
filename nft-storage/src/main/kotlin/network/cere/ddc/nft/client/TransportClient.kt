package network.cere.ddc.nft.client

import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.Metadata

interface TransportClient {

    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath
    suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray
    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath
    suspend fun readMetadata(nftId: String, nftPath: NftPath): ByteArray

}