package network.cere.ddc.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import network.cere.ddc.core.signature.Scheme
import network.cere.ddc.storage.config.FileStorageConfig
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

internal class FileStorageIT {

    private val privateKey = "0x2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03"
    private val gatewayNodeUrl = "http://localhost:8080"
    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val testSubject =
        FileStorage(scheme, gatewayNodeUrl, FileStorageConfig().copy(parallel = 2, chunkSizeInBytes = 2))

    private val path = Paths.get("src", "test", "resources", "test.txt")

    @Test
    fun `Store and read`(): Unit = runBlocking {
        //given
        val fileBytes = Files.readAllBytes(path)

        //when
        val uri = testSubject.upload(1L, path)
        val data = testSubject.read(1L, uri.cid)

        //then
        data shouldBe fileBytes
    }

    @Test
    fun `Store and download`(): Unit = runBlocking {
        //given
        val fileBytes = Files.readAllBytes(path)
        val newFile = Paths.get("src", "test", "resources", "test1.txt")
            //Files.createTempFile("file_storage_download_test", null).toAbsolutePath()

        //when
        val uri = testSubject.upload(2L, path)
        testSubject.download(2L, uri.cid, newFile)

        //then
        Files.readAllBytes(newFile) shouldBe fileBytes

    }

}