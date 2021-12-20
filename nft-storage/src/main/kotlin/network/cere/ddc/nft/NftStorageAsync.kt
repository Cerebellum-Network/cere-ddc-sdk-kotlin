package network.cere.ddc.nft

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import java.util.concurrent.CompletableFuture

class NftStorageAsync(private val client: TransportClient) {

    fun storeAsset(nftId: String, data: ByteArray, name: String): CompletableFuture<NftPath> =
        GlobalScope.async { client.storeAsset(nftId, data, name) }.asCompletableFuture()

    fun readAsset(nftId: String, nftPath: NftPath): CompletableFuture<ByteArray> =
        GlobalScope.async { client.readAsset(nftId, nftPath) }.asCompletableFuture()

    fun storeMetadata(nftId: String, metadata: Metadata): CompletableFuture<NftPath> =
        GlobalScope.async { client.storeMetadata(nftId, metadata) }.asCompletableFuture()

    fun <T : Metadata> readMetadata(nftId: String, nftPath: NftPath, schema: Class<T>): CompletableFuture<T> =
        GlobalScope.async { client.readMetadata(nftId, nftPath, schema) }.asCompletableFuture()
}