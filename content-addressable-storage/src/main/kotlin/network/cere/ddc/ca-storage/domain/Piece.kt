package network.cere.ddc.`ca-storage`.domain

import com.google.protobuf.ByteString
import pb.LinkOuterClass
import pb.PieceOuterClass
import pb.TagOuterClass

data class Piece(
    var data: ByteArray,
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

    fun toProto(bucketId: Long): PieceOuterClass.Piece {
        val protoTags = tags.map {
            TagOuterClass.Tag.newBuilder()
                .setKey(ByteString.copyFrom(it.key.toByteArray()))
                .setValue(ByteString.copyFrom(it.value.toByteArray()))
                .setSearchable(TagOuterClass.SearchType.valueOf(it.searchType.name))
                .build()
        }.asIterable()
        val protoLinks = links.map {
            LinkOuterClass.Link.newBuilder()
                .setCid(it.cid)
                .setName(it.name)
                .setSize(it.size)
                .build()
        }.asIterable()
        return PieceOuterClass.Piece.newBuilder()
            .setBucketId(bucketId.toInt())
            .setData(ByteString.copyFrom(data))
            .addAllTags(protoTags)
            .addAllLinks(protoLinks)
            .build()
    }
}
