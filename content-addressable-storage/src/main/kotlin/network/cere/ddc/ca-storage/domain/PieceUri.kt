package network.cere.ddc.`ca-storage`.domain

data class PieceUri(val bucketId: Long, val cid: String) {
   override fun toString() = "cns://$bucketId/$cid"
}
