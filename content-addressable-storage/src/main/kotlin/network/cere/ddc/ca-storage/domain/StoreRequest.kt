package network.cere.ddc.`ca-storage`.domain

data class StoreRequest(
    val body: ByteArray,
    val cid: String,
    val method: String,
    val path: String,
)
