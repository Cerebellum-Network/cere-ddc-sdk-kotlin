package network.cere.ddc.core.cid

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class CidBuilderTest {

    private val testSubject = CidBuilder()

    @Test
    fun `Build CID`() {
        //given
        val expectedCid = "bafkreigaknpexyvxt76zgkitavbwx6ejgfheup5oybpm77f3pxzrvwpfdi"

        //when
        val cid = testSubject.build("Hello world!".toByteArray())

        //then
        assertEquals(expectedCid, cid)
    }
}
