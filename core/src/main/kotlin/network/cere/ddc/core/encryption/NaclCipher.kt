package network.cere.ddc.core.encryption

import org.apache.tuweni.crypto.sodium.SecretBox.Key
import org.apache.tuweni.crypto.sodium.SecretBox.Nonce
import org.apache.tuweni.crypto.sodium.SecretBox.decrypt
import org.apache.tuweni.crypto.sodium.SecretBox.encrypt
import org.bouncycastle.jcajce.provider.digest.Blake2b

class NaclCipher : Cipher {
    override suspend fun encrypt(data: ByteArray, dek: ByteArray, nonce: Nonce): ByteArray {
        return encrypt(data, Key.fromBytes(blake2b256Hash(dek)), nonce)
    }

    override suspend fun decrypt(data: ByteArray, dek: ByteArray, nonce: Nonce): ByteArray? {
        return decrypt(data, Key.fromBytes(blake2b256Hash(dek)), nonce)
    }

    private fun blake2b256Hash(data: ByteArray): ByteArray {
        return Blake2b.Blake2b256().apply { this.update(data) }.digest()
    }
}