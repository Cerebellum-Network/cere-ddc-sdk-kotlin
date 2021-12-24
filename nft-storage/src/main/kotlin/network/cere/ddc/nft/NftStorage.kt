package network.cere.ddc.nft

import network.cere.ddc.core.extension.retry
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.config.NftStorageConfig
import network.cere.ddc.nft.exception.*
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Metadata
import org.slf4j.LoggerFactory

class NftStorage(private val client: TransportClient, private val config: NftStorageConfig = NftStorageConfig()) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath = try {
        retry("Couldn't store asset to NFT Storage") { client.storeAsset(nftId, data, name) }
    } catch (e: Exception) {
        throw AssetSaveNftException("Couldn't store asset", e)
    }

    suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray = try {
        retry("Couldn't read asset from NFT Storage") { client.readAsset(nftId, parseCid(nftPath)) }
    } catch (e: Exception) {
        throw AssetReadNftException("Couldn't read asset", e)
    }

    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath = try {
        retry("Couldn't store metadata to NFT Storage") { client.storeMetadata(nftId, metadata) }
    } catch (e: Exception) {
        throw MetadataSaveNftException("Couldn't store metadata", e)
    }

    suspend fun readMetadata(nftId: String, nftPath: NftPath): Metadata = try {
        retry("Couldn't read metadata from NFT Storage") { client.readMetadata(nftId, parseCid(nftPath)) }
    } catch (e: Exception) {
        throw MetadataReadNftException("Couldn't read metadata", e)
    }

    suspend fun storeEdek(nftId: String, metadataNftPath: NftPath, edek: Edek): Edek = try {
        retry("Couldn't store EDEK to NFT Storage") { client.storeEdek(nftId, parseCid(metadataNftPath), edek) }
    } catch (e: Exception) {
        throw EdekSaveNftException("Couldn't store EDEK", e)
    }

    suspend fun readEdek(nftId: String, metadataNftPath: NftPath, publicKeyHex: String): Edek = try {
        retry("Couldn't read EDEK from NFT Storage") { client.readEdek(nftId, parseCid(metadataNftPath), publicKeyHex) }
    } catch (e: Exception) {
        throw EdekReadNftException("Couldn't read EDEK", e)
    }

    private fun parseCid(nftPath: NftPath): String {
        val path = nftPath.url!!.split("/")

        if (path.size != 5 || path[3].trim().isEmpty()) {
            throw IllegalArgumentException("Invalid NFT path url")
        }

        return path[3]
    }

    private suspend inline fun <reified R> retry(message: String, action: () -> R) =
        retry(config.retryTimes, config.retryBackOff, {
            logger.warn("{}. Exception message: {}", message, it.message)
            it is NftException
        }, action)
}