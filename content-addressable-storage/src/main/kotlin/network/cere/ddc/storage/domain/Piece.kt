package network.cere.ddc.storage.domain

data class Piece(
    val data: ByteArray,
    val tags: List<Tag> = listOf(),
    val links: List<Link> = listOf(),
    val cid: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Piece

        if (!data.contentEquals(other.data)) return false
        if (tags != other.tags) return false
        if (links != other.links) return false
        if (cid != other.cid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + links.hashCode()
        result = 31 * result + (cid?.hashCode() ?: 0)
        return result
    }
}
