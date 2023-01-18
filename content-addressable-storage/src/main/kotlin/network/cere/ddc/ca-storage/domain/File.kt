package network.cere.ddc.`ca-storage`.domain

import java.nio.file.Path

data class File(
    var data: ByteArray? = null,
    val filePath: Path? = null,
    val tags: List<Tag> = listOf(),
    val cid: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (filePath != other.filePath) return false
        if (tags != other.tags) return false
        if (cid != other.cid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + tags.hashCode()
        result = 31 * result + (cid?.hashCode() ?: 0)
        return result
    }
}