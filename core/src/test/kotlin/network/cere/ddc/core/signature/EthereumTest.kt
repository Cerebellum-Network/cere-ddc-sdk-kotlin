package network.cere.ddc.core.signature

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EthereumTest {

    private val signature =
        "0x789a80053e4927d0a898db8e065e948f5cf086e32f9ccaa54c1908e22ac430c62621578113ddbb62d509bf6049b8fb544ab06d36f916685a2eb8e57ffadde0231c"

    private val testSubject = Ethereum("fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19")

    @Test
    fun `Valid signature`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        assertThat(result).isTrue
    }

    @Test
    fun `Invalid signature`() {
        //given
        val message = "test"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        assertThat(result).isFalse
    }

    @Test
    fun `Sign data`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.sign(message.toByteArray())

        //then
        assertThat(result).isEqualTo(signature)
    }
}