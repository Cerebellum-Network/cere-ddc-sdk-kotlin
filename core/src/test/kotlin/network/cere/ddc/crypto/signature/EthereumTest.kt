package network.cere.ddc.crypto.signature

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EthereumTest {
    private val testSubject = Ethereum("0x1Bf6FCa28253A1257e4B5B3440F7fbE0c59D1546")

    @Test
    fun `Valid signature`() {
        //given
        val signature =
            "0x77279fb65cc52f46787faaee1eac38c701feeb5fd1f33b424ca6e5b5b21d65a108a27d5b9835ae14eec1c7065987928e3e95dfb97ffba66e51829daec3a5f2691c"
        val message = "Test"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        assertThat(result).isTrue
    }

    @Test
    fun `Invalid signature`() {
        //given
        val signature =
            "0x77279fb65cc52f46787faaee1eac38c701feeb5fd1f33b424ca6e5b5b21d65a108a27d5b9835ae14eec1c7065987928e3e95dfb97ffba66e51829daec3a5f2691d"
        val message = "Test"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        assertThat(result).isFalse
    }
}