package network.cere.ddc.core.encryption

import org.apache.tuweni.crypto.sodium.SecretBox.Key
import org.apache.tuweni.crypto.sodium.SecretBox.Nonce
import org.apache.tuweni.crypto.sodium.SecretBox.decrypt
import org.apache.tuweni.crypto.sodium.SecretBox.encrypt
import java.util.*

class NaclCipher : Cipher {
    override fun encrypt(data: ByteArray, dek: ByteArray): EncryptedData {
        val nonceString = UUID.randomUUID().toString().substring(0, 24) //TODO change such a weired implementation, when Tag will ba able to store ByteArray
        return EncryptedData(encrypt(data, Key.fromBytes(dek), Nonce.fromBytes(nonceString.toByteArray())), nonceString.toByteArray())
    }

    override fun decrypt(encryptedData: EncryptedData, dek: ByteArray): ByteArray {
        return decrypt(encryptedData.data, Key.fromBytes(dek), Nonce.fromBytes(encryptedData.nonce)) ?: throw Exception("Can't decrypt data!")
    }
}