package network.cere.ddc.`object`

import network.cere.ddc.`object`.client.TransportClient
import network.cere.ddc.`object`.config.ObjectStorageConfig
import network.cere.ddc.`object`.exception.*
import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath
import network.cere.ddc.core.extension.retry
import org.slf4j.LoggerFactory

class ObjectStorage(
    private val client: TransportClient,
    private val config: ObjectStorageConfig = ObjectStorageConfig()
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Store data to Object Storage. Retries on exception.
     *
     * @param bucketId bucket identifier where store data
     * @param data encrypted bytes of data
     *
     * @throws SaveObjectException on failed storing after all retries
     *
     * @return path with CID to stored data
     */
    suspend fun storeObject(bucketId: String, data: ByteArray): ObjectPath = try {
        retry("Couldn't store data to Object Storage") { client.storeObject(bucketId, data) }
    } catch (e: Exception) {
        throw SaveObjectException("Couldn't store data", e)
    }

    /**
     * Read data from Object Storage. Retries on exception.
     *
     * @param bucketId bucket identifier where stored data
     * @param objectPath path with CID to stored data
     *
     * @throws ReadObjectException on failed reading after all retries
     *
     * @return encrypted bytes of data
     */
    suspend fun readObject(bucketId: String, objectPath: ObjectPath): ByteArray = try {
        retry("Couldn't read data from Object Storage") { client.readObject(bucketId, parseCid(objectPath)) }
    } catch (e: Exception) {
        throw ReadObjectException("Couldn't read data", e)
    }

    /**
     * Save EDEK to Object Storage. Retries on exception.
     *
     * @param bucketId bucket identifier where stored metadata and store EDEK
     * @param objectPath Object path with CID to metadata for Object
     * @param edek Object path with CID to metadata in Object Storage
     *
     * @throws EdekSaveObjectException on failed storing after all retries
     *
     * @return stored EDEK in Object Storage
     */
    suspend fun storeEdek(bucketId: String, objectPath: ObjectPath, edek: Edek): Edek = try {
        retry("Couldn't store EDEK to Object Storage") { client.storeEdek(bucketId, parseCid(objectPath), edek) }
    } catch (e: Exception) {
        throw EdekSaveObjectException("Couldn't store EDEK", e)
    }

    /**
     * Read EDEK from Object Storage. Retries on exception.
     *
     * @param bucketId bucket identifier where stored EDEK
     * @param objectPath path with CID to metadata for Object
     * @param publicKeyHex EDEK public key in Hex format
     *
     * @throws EdekReadObjectException on failed reading after all retries
     *
     * @return EDEK from Object Storage
     */
    suspend fun readEdek(bucketId: String, objectPath: ObjectPath, publicKeyHex: String): Edek = try {
        retry("Couldn't read EDEK from Object Storage") {
            client.readEdek(bucketId, parseCid(objectPath), publicKeyHex)
        }
    } catch (e: Exception) {
        throw EdekReadObjectException("Couldn't read EDEK", e)
    }

    private fun parseCid(objectPath: ObjectPath): String {
        val path = objectPath.url!!.split("/")

        if (path.size != 4 || path[3].trim().isEmpty()) {
            throw IllegalArgumentException("Invalid Object path url")
        }

        return path[3]
    }

    private suspend inline fun <reified R> retry(message: String, action: () -> R) =
        retry(config.retryTimes, config.retryBackOff, {
            logger.warn("{}. Exception message: {}", message, it.message)
            it is ObjectException
        }, action)
}