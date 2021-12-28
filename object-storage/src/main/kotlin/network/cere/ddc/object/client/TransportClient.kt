package network.cere.ddc.`object`.client

import network.cere.ddc.`object`.model.Edek
import network.cere.ddc.`object`.model.ObjectPath

interface TransportClient : AutoCloseable {

    suspend fun storeObject(bucketId: String, data: ByteArray): ObjectPath
    suspend fun readObject(bucketId: String, cid: String): ByteArray
    suspend fun storeEdek(bucketId: String, cid: String, edek: Edek): Edek
    suspend fun readEdek(bucketId: String, cid: String, publicKeyHex: String): Edek

}