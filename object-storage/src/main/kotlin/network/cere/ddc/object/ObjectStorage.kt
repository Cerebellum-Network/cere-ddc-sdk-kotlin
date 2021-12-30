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
    suspend fun storeObject(bucketId: Long, data: ByteArray): ObjectPath = try {
        retry("Couldn't store data to Object Storage") { client.storeObject(bucketId, data) }
    } catch (e: Exception) {
        throw SaveObjectException("Couldn't store data", e)
    }

    /**
     * Read data from Object Storage. Retries on exception.
     *
     * @param objectPath path with CID and bucket identifier to stored data
     *
     * @throws ReadObjectException on failed reading after all retries
     *
     * @return encrypted bytes of data
     */
    suspend fun readObject(objectPath: ObjectPath): ByteArray = try {
        retry("Couldn't read data from Object Storage") {
            client.readObject(objectPath)
        }
    } catch (e: Exception) {
        throw ReadObjectException("Couldn't read data", e)
    }

    /**
     * Save EDEK to Object Storage. Retries on exception.
     *
     * @param objectPath path with CID and bucket identifier for Object
     * @param edek Object path with CID to metadata in Object Storage
     *
     * @throws EdekSaveObjectException on failed storing after all retries
     *
     * @return stored EDEK in Object Storage
     */
    suspend fun storeEdek(objectPath: ObjectPath, edek: Edek): Edek = try {
        retry("Couldn't store EDEK to Object Storage") {
            client.storeEdek(objectPath, edek)
        }
    } catch (e: Exception) {
        throw EdekSaveObjectException("Couldn't store EDEK", e)
    }

    /**
     * Read EDEK from Object Storage. Retries on exception.
     *
     * @param objectPath path with CID and bucket identifier for Object
     * @param publicKeyHex EDEK public key in Hex format
     *
     * @throws EdekReadObjectException on failed reading after all retries
     *
     * @return EDEK from Object Storage
     */
    suspend fun readEdek(objectPath: ObjectPath, publicKeyHex: String): Edek = try {
        retry("Couldn't read EDEK from Object Storage") {
            client.readEdek(objectPath, publicKeyHex)
        }
    } catch (e: Exception) {
        throw EdekReadObjectException("Couldn't read EDEK", e)
    }

    private suspend inline fun <reified R> retry(message: String, action: () -> R) =
        retry(config.retryTimes, config.retryBackOff, {
            logger.warn("{}. Exception message: {}", message, it.message)
            it is ObjectException
        }, action)
}