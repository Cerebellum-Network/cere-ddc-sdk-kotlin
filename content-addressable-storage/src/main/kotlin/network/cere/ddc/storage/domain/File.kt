package network.cere.ddc.storage.domain

data class File (
    override var data: ByteArray,
    override val tags: List<Tag> = listOf(),
    override val cid: String? = null
): Container {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (!data.contentEquals(other.data)) return false
        if (tags != other.tags) return false
        if (cid != other.cid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (cid?.hashCode() ?: 0)
        return result
    }
}
