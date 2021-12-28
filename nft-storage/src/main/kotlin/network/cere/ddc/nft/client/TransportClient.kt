package network.cere.ddc.nft.client

import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata

interface TransportClient : AutoCloseable {

    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath
    suspend fun readAsset(nftId: String, cid: String): ByteArray
    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath
    suspend fun readMetadata(nftId: String, cid: String): Metadata
    suspend fun storeEdek(nftId: String, cid: String, edek: Edek): Edek
    suspend fun readEdek(nftId: String, cid: String, publicKeyHex: String): Edek

}