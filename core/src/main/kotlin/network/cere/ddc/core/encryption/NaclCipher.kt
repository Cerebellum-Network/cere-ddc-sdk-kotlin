package network.cere.ddc.core.encryption

import org.apache.tuweni.crypto.sodium.SecretBox.Key
import org.apache.tuweni.crypto.sodium.SecretBox.Nonce
import org.apache.tuweni.crypto.sodium.SecretBox.decrypt
import org.apache.tuweni.crypto.sodium.SecretBox.encrypt

class NaclCipher : Cipher {
    override fun encrypt(data: ByteArray, dek: ByteArray): EncryptedData {
        val nonce = Nonce.random()
        return EncryptedData(encrypt(data, Key.fromBytes(dek), nonce), nonce.bytesArray())
    }

    override fun decrypt(encryptedData: EncryptedData, dek: ByteArray): ByteArray {
        return decrypt(encryptedData.data, Key.fromBytes(dek), Nonce.fromBytes(encryptedData.nonce)) ?: throw RuntimeException("Can't decrypt data!")
    }
}