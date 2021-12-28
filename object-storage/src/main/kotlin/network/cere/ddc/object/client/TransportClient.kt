package network.cere.ddc.`object`.client

import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath

interface TransportClient : AutoCloseable {

    suspend fun storeObject(bucketId: String, data: ByteArray): ObjectPath
    suspend fun readObject(objectPath: ObjectPath): ByteArray
    suspend fun storeEdek(objectPath: ObjectPath, edek: Edek): Edek
    suspend fun readEdek(objectPath: ObjectPath, publicKeyHex: String): Edek

}