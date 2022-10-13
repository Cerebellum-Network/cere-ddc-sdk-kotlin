package network.cere.ddc.core.encryption

interface Cipher {
    fun encrypt(data: ByteArray, dek: ByteArray): EncryptedData
    fun decrypt(encryptedData: EncryptedData, dek: ByteArray): ByteArray
    fun getEmptyNonce(): ByteArray
}
//TODO think about implementation with constant nonce