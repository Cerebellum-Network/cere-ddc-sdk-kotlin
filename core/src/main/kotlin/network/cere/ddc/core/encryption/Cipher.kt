package network.cere.ddc.core.encryption

import org.apache.tuweni.crypto.sodium.SecretBox

interface Cipher {
    suspend fun encrypt(data: ByteArray, dek: ByteArray, nonce: SecretBox.Nonce): ByteArray

    suspend fun decrypt(data: ByteArray, dek: ByteArray, nonce: SecretBox.Nonce): ByteArray?
}