package network.cere.ddc.storage.file

data class ChunkData(
    val position: Long,
    val data: ByteArray
)
