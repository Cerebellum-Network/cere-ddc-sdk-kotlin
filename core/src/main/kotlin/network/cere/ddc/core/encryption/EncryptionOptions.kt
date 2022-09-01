package network.cere.ddc.core.encryption

data class EncryptionOptions(
    val dekPath: String,
    val dek: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionOptions

        if (dekPath != other.dekPath) return false
        if (!dek.contentEquals(other.dek)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dekPath.hashCode()
        result = 31 * result + dek.contentHashCode()
        return result
    }
}
