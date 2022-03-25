package network.cere.ddc.storage.config

data class FileStorageConfig(
    val parallel: Int = 4,
    val chunkSizeInBytes: Int = 5 * MB,
    val clientConfig: ClientConfig = ClientConfig(),
) {
    companion object {
        const val KB = 1024
        const val MB = 1024 * KB
    }
}
