package network.cere.ddc.`file-storage`.config

import network.cere.ddc.`ca-storage`.config.ClientConfig

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
