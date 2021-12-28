package network.cere.ddc.nft

import network.cere.ddc.core.extension.retry
import network.cere.ddc.nft.client.TransportClient
import network.cere.ddc.nft.config.NftStorageConfig
import network.cere.ddc.nft.exception.*
import network.cere.ddc.nft.model.Edek
import network.cere.ddc.nft.model.NftPath
import network.cere.ddc.nft.model.metadata.Erc1155Metadata
import network.cere.ddc.nft.model.metadata.Erc721Metadata
import network.cere.ddc.nft.model.metadata.Metadata
import org.slf4j.LoggerFactory

class NftStorage(private val client: TransportClient, private val config: NftStorageConfig = NftStorageConfig()) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Store asset to NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where store asset (can be many for 1 user)
     * @param data encrypted bytes of asset
     * @param name filename of asset (used for building [NftPath])
     *
     * @throws AssetSaveNftException on failed storing after all retries
     *
     * @return path with CID to stored asset
     */
    suspend fun storeAsset(nftId: String, data: ByteArray, name: String): NftPath = try {
        retry("Couldn't store asset to NFT Storage") { client.storeAsset(nftId, data, name) }
    } catch (e: Exception) {
        throw AssetSaveNftException("Couldn't store asset", e)
    }

    /**
     * Read asset from NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where stored asset (can be many for 1 user)
     * @param nftPath path with CID to stored asset
     *
     * @throws AssetReadNftException on failed reading after all retries
     *
     * @return encrypted bytes of asset
     */
    suspend fun readAsset(nftId: String, nftPath: NftPath): ByteArray = try {
        retry("Couldn't read asset from NFT Storage") { client.readAsset(nftId, parseCid(nftPath)) }
    } catch (e: Exception) {
        throw AssetReadNftException("Couldn't read asset", e)
    }

    /**
     * Save metadata for NFT to NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where store metadata (can be many for 1 user)
     * @param metadata NFT metadata for storing in ERC-721 or ERC-1155 format
     *
     * @throws MetadataSaveNftException on failed storing after all retries
     *
     * @return path with CID to stored metadata
     */
    suspend fun storeMetadata(nftId: String, metadata: Metadata): NftPath = try {
        retry("Couldn't store metadata to NFT Storage") { client.storeMetadata(nftId, metadata) }
    } catch (e: Exception) {
        throw MetadataSaveNftException("Couldn't store metadata", e)
    }

    /**
     * Read metadata from NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where stored metadata (can be many for 1 user)
     * @param nftPath path with CID to stored metadata
     *
     * @throws MetadataReadNftException on failed reading after all retries
     *
     * @return NFT metadata ([Erc721Metadata] or [Erc1155Metadata])
     */
    suspend fun readMetadata(nftId: String, nftPath: NftPath): Metadata = try {
        retry("Couldn't read metadata from NFT Storage") { client.readMetadata(nftId, parseCid(nftPath)) }
    } catch (e: Exception) {
        throw MetadataReadNftException("Couldn't read metadata", e)
    }

    /**
     * Save NFT EDEK to NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where stored metadata and store EDEK (can be many for 1 user)
     * @param metadataNftPath NFT path with CID to metadata for NFT
     * @param edek NFT path with CID to metadata in NFT Storage
     *
     * @throws EdekSaveNftException on failed storing after all retries
     *
     * @return stored EDEK in NFT Storage
     */
    suspend fun storeEdek(nftId: String, metadataNftPath: NftPath, edek: Edek): Edek = try {
        retry("Couldn't store EDEK to NFT Storage") { client.storeEdek(nftId, parseCid(metadataNftPath), edek) }
    } catch (e: Exception) {
        throw EdekSaveNftException("Couldn't store EDEK", e)
    }

    /**
     * Read EDEK from NFT Storage. Retries on exception.
     *
     * @param nftId bucket identifier where stored EDEK (can be many for 1 user)
     * @param nftPath path with CID to metadata for NFT
     * @param publicKeyHex EDEK public key in Hex format
     *
     * @throws EdekReadNftException on failed reading after all retries
     *
     * @return EDEK from NFT Storage
     */
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