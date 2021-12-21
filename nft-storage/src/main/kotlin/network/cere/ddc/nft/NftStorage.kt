package network.cere.ddc.nft

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import java.util.concurrent.CompletableFuture

@DelicateCoroutinesApi
class NftStorage(private val client: TransportClient) {

    fun storeAsset(nftId: String, data: ByteArray, name: String): CompletableFuture<NftPath> =
        GlobalScope.future { client.storeAsset(nftId, data, name) }

    fun readAsset(nftId: String, nftPath: NftPath): CompletableFuture<ByteArray> =
        GlobalScope.future { client.readAsset(nftId, nftPath) }

    fun storeMetadata(nftId: String, metadata: Metadata): CompletableFuture<NftPath> =
        GlobalScope.future { client.storeMetadata(nftId, metadata) }

    fun <T : Metadata> readMetadata(nftId: String, nftPath: NftPath, schema: Class<T>): CompletableFuture<T> =
        GlobalScope.future { client.readMetadata(nftId, nftPath, schema) }
}