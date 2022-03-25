package network.cere.ddc.storage

import network.cere.ddc.core.signature.Scheme
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class FileStorageIT {

    private val privateKey =
        "0x500c89905aba00fc5211a2876b6105001ad8c77218b37b649ee38b4996e0716d6d5c7e03bbedbf39ca3588b5e6e0768e70a4507cd9e089f625929f9efae051f2"

    private val scheme = Scheme.create(Scheme.SR_25519, privateKey)
    private val gatewayNodeUrl = "http://localhost:8080"
    private val testSubject = FileStorage(scheme, gatewayNodeUrl)

    @Test
    fun `Store and read`() {
        //given
        val resourceDirectory = Paths.get("file-storage","src", "test", "resources", )

        //when

        //then
    }

    @Test
    fun `Store and download`() {
        //given

        //when

        //then
    }

}