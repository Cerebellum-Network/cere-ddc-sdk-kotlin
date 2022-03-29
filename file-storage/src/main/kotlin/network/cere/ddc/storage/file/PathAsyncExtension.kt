package network.cere.ddc.storage.file

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.suspendCoroutine

suspend fun Path.writeFromChannel(channel: ReceiveChannel<ChunkData>) {
    runCatching {
        AsynchronousFileChannel.open(this, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
            .use { fileChannel ->
                val handler = AsynchronousFileHandler()

                channel.consumeEach { positionedByteArray ->
                    val bytes = positionedByteArray.data
                    val buffer = ByteBuffer.allocate(bytes.size)
                    buffer.put(bytes)
                    buffer.flip()

                    suspendCoroutine<Int> { fileChannel.write(buffer, positionedByteArray.position, it, handler) }
                }
            }
    }.onFailure { channel.cancel(CancellationException(it)) }
}

suspend fun Path.readToChannel(bufferSize: Int, channel: SendChannel<ByteArray>) {
    runCatching {
        AsynchronousFileChannel.open(this@readToChannel, StandardOpenOption.READ).use { fileChannel ->
            val handler = AsynchronousFileHandler()
            val buffer = ByteBuffer.allocate(bufferSize)

            var position = 0L
            while (true) {
                val readBytes = suspendCoroutine<Int> { fileChannel.read(buffer, position, it, handler) }.toLong()

                if (readBytes < 0) {
                    break
                }

                position += readBytes
                buffer.flip()
                channel.send(buffer.array().copyOf(buffer.remaining()))
                buffer.clear()
            }
        }
    }.exceptionOrNull().also { channel.close(it) }
}
