package network.cere.ddc.nft

import network.cere.ddc.crypto.signature.Scheme
import network.cere.ddc.nft.model.Metadata

class NftStorage(scheme: Scheme) {

    fun storeAsset(nftId: String, data: ByteArray, name: String) {
        TODO("Not yet implemented")
    }

    fun readAsset(url: String) {
        TODO("Not yet implemented")
    }

    fun storeMetadata(nftId: String, metadata: Metadata) {
        TODO("Not yet implemented")
    }

    fun readMetadata(url: String) {
        TODO("Not yet implemented")
    }
}