package network.cere.ddc.`file-storage`.file

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class PathAsyncExtensionTest {

    @Test
    fun `Read file`(): Unit = runBlocking {
        //given
        val path = Paths.get("src", "test", "resources", "test.txt")
        val byteChannel = Channel<ByteArray>()

        //when
        launch { path.readToChannel(3, byteChannel) }

        //then
        var result = byteArrayOf()
        for (array in byteChannel) {
            result += array
        }

        result shouldBe Files.readAllBytes(path)
    }


    @Test
    fun `Write file`(): Unit = runBlocking {
        //given
        val bytes = "hello test world".toByteArray()
        val file = Files.createTempFile("file_storage_write_file_test", null)
        val channel = Channel<ChunkData>()
        launch {
            bytes.toList().asSequence()
                .chunked(3)
                .map { it.toByteArray() }
                .fold(0L) { pos, value ->
                    channel.send(ChunkData(pos, value))
                    pos + value.size
                }
            channel.close()
        }

        //when
        val job = launch { file.toAbsolutePath().writeFromChannel(channel) }
        job.join()

        //then
        Files.readAllBytes(file) shouldBe bytes
    }
}
