package network.cere.ddc.storage.domain

data class PieceUri(val bucketId: Long, val cid: String) {
   override fun toString() = "cns://$bucketId/$cid"
}
