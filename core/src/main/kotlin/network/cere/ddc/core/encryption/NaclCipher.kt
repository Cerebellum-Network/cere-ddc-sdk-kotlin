package network.cere.ddc.core.encryption

import com.iwebpp.crypto.TweetNaclFast.SecretBox

class NaclCipher : Cipher {
    override fun encrypt(data: ByteArray, dek: ByteArray): EncryptedData {
        val nonce = (0..Long.MAX_VALUE).random()
        val secretBox = SecretBox(dek, nonce)
        return EncryptedData(secretBox.box(data), nonce.toString().encodeToByteArray())
    }

    override fun decrypt(encryptedData: EncryptedData, dek: ByteArray): ByteArray {
        val secretBox = SecretBox(dek, encryptedData.nonce.decodeToString().toLong())
        secretBox.open(encryptedData.data)
        return secretBox.open(encryptedData.data)
    }

    override fun getEmptyNonce(): ByteArray {
        return ByteArray(SecretBox.nonceLength)
    }
}