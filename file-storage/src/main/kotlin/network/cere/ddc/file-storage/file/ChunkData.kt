package network.cere.ddc.`file-storage`.file

data class ChunkData(
    val position: Long,
    val data: ByteArray
)
