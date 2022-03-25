package network.cere.ddc.storage.domain

data class Piece(val data: ByteArray, val tags: List<Tag> = listOf(), val links: List<Link> = listOf()) {

}
