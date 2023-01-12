package network.cere.ddc.`file-storage`

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.encryption.EncryptionOptions
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.`ca-storage`.ContentAddressableStorage
import network.cere.ddc.`file-storage`.config.FileStorageConfig
import network.cere.ddc.`ca-storage`.domain.CreateSessionParams
import network.cere.ddc.`ca-storage`.domain.Link
import network.cere.ddc.`ca-storage`.domain.Piece
import network.cere.ddc.`file-storage`.file.ChunkData
import network.cere.ddc.`file-storage`.file.readToChannel
import network.cere.ddc.`file-storage`.file.writeFromChannel
import java.nio.file.Path
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.io.path.name

class FileStorage{
    private val caStorage : ContentAddressableStorage
    private val fileStorageConfig : FileStorageConfig
    constructor(
        caStorage : ContentAddressableStorage,
        fileStorageConfig: FileStorageConfig = FileStorageConfig(),
    ) {
        this.caStorage = caStorage
        this.fileStorageConfig = fileStorageConfig
    }
    constructor(
        scheme: Scheme,
        cdnNodeUrl: String,
        fileStorageConfig: FileStorageConfig = FileStorageConfig(),
        cidBuilder: CidBuilder = CidBuilder(),
    ) {
        this.caStorage = ContentAddressableStorage(scheme, cdnNodeUrl, fileStorageConfig.clientConfig, cidBuilder)
        this.fileStorageConfig = fileStorageConfig
    }

    suspend fun upload(bucketId: Long, file: Path): DdcUri = coroutineScope {
        val channelBytes = Channel<ByteArray>(fileStorageConfig.parallel)
        val indexedBytes = indexBytes(channelBytes)
        val linksSet = ConcurrentSkipListSet<Pair<Long, Link>>(Comparator.comparingLong { it.first })
        val name = file.name

        //Connect bytes channel with file
        launch { file.readToChannel(fileStorageConfig.chunkSizeInBytes, channelBytes) }

        //Upload pieces and create link object
        val jobs = (0 until fileStorageConfig.parallel).map {
            launch {
                indexedBytes.consumeEach {
                    val pieceUri = caStorage.store(bucketId, Piece(data = it.second))
                    linksSet.add(
                        it.first to Link(
                            cid = pieceUri.cid,
                            size = it.second.size.toLong()
                        )
                    )
                }
            }
        }

        jobs.forEach { it.join() }
        val links = linksSet.map { it.second }

        caStorage.store(bucketId, Piece(name.toByteArray(), links = links))
    }

    suspend fun uploadEncrypted(bucketId: Long, file: Path, encryptionOptions: EncryptionOptions): DdcUri = coroutineScope {
        val channelBytes = Channel<ByteArray>(fileStorageConfig.parallel)
        val indexedBytes = indexBytes(channelBytes)
        val linksSet = ConcurrentSkipListSet<Pair<Long, Link>>(Comparator.comparingLong { it.first })
        val name = file.name

        //Connect bytes channel with file
        launch { file.readToChannel(fileStorageConfig.chunkSizeInBytes, channelBytes) }

        //Upload pieces and create link object
        val jobs = (0 until fileStorageConfig.parallel).map {
            launch {
                indexedBytes.consumeEach {
                    val pieceUri = caStorage.storeEncrypted(bucketId, Piece(data = it.second), encryptionOptions)
                    linksSet.add(
                        it.first to Link(
                            cid = pieceUri.cid,
                            size = it.second.size.toLong()
                        )
                    )
                }
            }
        }

        jobs.forEach { it.join() }
        val links = linksSet.map { it.second }

        caStorage.storeEncrypted(bucketId, Piece(name.toByteArray(), links = links), encryptionOptions)
    }

    suspend fun read(bucketId: Long, cid: String, session: ByteArray? = null): ByteArray = coroutineScope {
        readToChannel(bucketId, null, cid, session).toList()
            .asSequence()
            .sortedBy { it.position }
            .fold(byteArrayOf()) { arr, value -> arr + value.data }
    }

    suspend fun readDecrypted(bucketId: Long, dek: ByteArray, cid: String, session: ByteArray? = null): ByteArray = coroutineScope {
        readToChannel(bucketId, dek, cid, session).toList()
            .asSequence()
            .sortedBy { it.position }
            .fold(byteArrayOf()) { arr, value -> arr + value.data }
    }

    suspend fun download(bucketId: Long, cid: String, file: Path, session: ByteArray? = null): Unit = coroutineScope {
        file.writeFromChannel(readToChannel(bucketId, null, cid, session))
    }

    suspend fun downloadDecrypted(bucketId: Long, dek: ByteArray, cid: String, file: Path, session: ByteArray? = null): Unit = coroutineScope {
        file.writeFromChannel(readToChannel(bucketId, dek, cid, session))
    }

    private fun CoroutineScope.readToChannel(bucketId: Long, dek: ByteArray?, cid: String, session: ByteArray? = null): ReceiveChannel<ChunkData> {
        val channel = Channel<ChunkData>(fileStorageConfig.parallel)
        launch {
            val readTaskChannel = Channel<ReadTask>(UNLIMITED)
            val headPiece = if (dek == null) caStorage.read(bucketId, cid, session) else caStorage.readDecrypted(bucketId, cid, dek)

            (0 until fileStorageConfig.parallel).map {
                launch {
                    readTaskChannel.consumeEach { task ->
                        val piece = if (dek == null) caStorage.read(bucketId,  task.link.cid, session) else caStorage.readDecrypted(bucketId,  task.link.cid, dek)

                        if (piece.data.size.toLong() != task.link.size) {
                            throw RuntimeException("Invalid piece size")
                        }

                        channel.send(ChunkData(task.position, piece.data))
                    }
                }
            }

            headPiece.links.fold(0L) { position, link ->
                readTaskChannel.send(ReadTask(position, link))
                position + link.size
            }

            readTaskChannel.close()
        }.invokeOnCompletion { channel.close(it) }

        return channel
    }

    private fun CoroutineScope.indexBytes(channel: ReceiveChannel<ByteArray>): ReceiveChannel<Pair<Long, ByteArray>> {
        val result = Channel<Pair<Long, ByteArray>>()

        launch {
            var index = 0L
            for (bytes in channel) {
                result.send(index++ to bytes)
            }
            result.close()
        }

        return result
    }

    private data class ReadTask(
        val position: Long,
        val link: Link
    )

    suspend fun createSession(createSessionParams: CreateSessionParams): ByteArray = caStorage.createSession(createSessionParams)

}
