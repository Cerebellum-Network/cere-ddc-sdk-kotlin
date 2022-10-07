package network.cere.ddc.storage.domain

import com.google.protobuf.ByteString
import network.cere.ddc.proto.Storage

data class Piece (
    override var data: ByteArray,
    override val tags: List<Tag> = listOf(),
    val links: List<Link> = listOf(),
    override val cid: String? = null
): Container {
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

    fun toProto(bucketId: Long): Storage.Piece {
        val protoTags = tags.map { Storage.Tag.newBuilder().setKey(it.key).setValue(it.value).build() }.asIterable()
        val protoLinks = links.map { Storage.Link.newBuilder().setCid(it.cid).setName(it.name).setSize(it.size).build() }.asIterable()
        return Storage.Piece.newBuilder().setBucketId(bucketId).setData(ByteString.copyFrom(data)).addAllTags(protoTags).addAllLinks(protoLinks).build()
    }
}
