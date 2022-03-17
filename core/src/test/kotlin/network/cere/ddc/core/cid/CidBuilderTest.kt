package network.cere.ddc.core.cid

import io.ipfs.multihash.Multihash
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CidBuilderTest {

    @Test
    fun `Build CID SHA-256`() {
        val testSubject = CidBuilder(type = Multihash.Type.sha2_256)

        //given
        val expectedCid = "bafkreigaknpexyvxt76zgkitavbwx6ejgfheup5oybpm77f3pxzrvwpfdi"

        //when
        val cid = testSubject.build("Hello world!".toByteArray())

        //then
        assertEquals(expectedCid, cid)
    }

    @Test
    fun `Build CID BLAKE2B-256`() {
        val testSubject = CidBuilder()

        //given
        val expectedCid = "bafk2bzacea73ycjnxe2qov7cvnhx52lzfp6nf5jcblnfus6gqreh6ygganbws"

        //when
        val cid = testSubject.build("Hello world!".toByteArray())

        //then
        assertEquals(expectedCid, cid)
    }

    @Test
    fun `Build CID UNKNOWN`() {
        //when
        val exception = assertThrows(RuntimeException::class.java) {
            CidBuilder(type = Multihash.Type.blake2b_104)
        }

        //then
        assertEquals("Unsupported multihash type", exception.message)
    }
}
