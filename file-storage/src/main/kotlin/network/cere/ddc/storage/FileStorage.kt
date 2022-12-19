package network.cere.ddc.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import network.cere.ddc.core.cid.CidBuilder
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.core.uri.DdcUri
import network.cere.ddc.storage.config.FileStorageConfig
import network.cere.ddc.storage.domain.Link
import network.cere.ddc.storage.domain.Piece
import network.cere.ddc.storage.file.ChunkData
import network.cere.ddc.storage.file.readToChannel
import network.cere.ddc.storage.file.writeFromChannel
import java.nio.file.Path
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.io.path.name

class FileStorage(
    scheme: Scheme,
    cdnNodeUrl: String,
    private val fileStorageConfig: FileStorageConfig = FileStorageConfig(),
    cidBuilder: CidBuilder = CidBuilder(),
) {

    private val caStorage =
        ContentAddressableStorage(scheme, cdnNodeUrl, fileStorageConfig.clientConfig, cidBuilder)

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

    suspend fun read(bucketId: Long, cid: String): ByteArray = coroutineScope {
        readToChannel(bucketId, cid).toList()
            .asSequence()
            .sortedBy { it.position }
            .fold(byteArrayOf()) { arr, value -> arr + value.data }
    }

    suspend fun download(bucketId: Long, cid: String, file: Path): Unit = coroutineScope {
        file.writeFromChannel(readToChannel(bucketId, cid))
    }

    private fun CoroutineScope.readToChannel(bucketId: Long, cid: String): ReceiveChannel<ChunkData> {
        val channel = Channel<ChunkData>(fileStorageConfig.parallel)
        launch {
            val readTaskChannel = Channel<ReadTask>(UNLIMITED)
            val headPiece = caStorage.read(bucketId, cid)

            (0 until fileStorageConfig.parallel).map {
                launch {
                    readTaskChannel.consumeEach { task ->
                        val piece = caStorage.read(bucketId, task.link.cid)

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
}
