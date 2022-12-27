package network.cere.ddc.storage.domain

data class StoreRequest(
    val body: ByteArray,
    val cid: String,
    val method: String,
    val path: String,
)
