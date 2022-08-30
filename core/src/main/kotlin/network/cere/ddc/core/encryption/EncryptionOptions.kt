package network.cere.ddc.core.encryption

data class EncryptionOptions(
    val dekPath: String,
    val dek: ByteArray,
)
