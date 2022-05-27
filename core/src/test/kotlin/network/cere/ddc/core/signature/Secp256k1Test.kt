package network.cere.ddc.core.signature

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import network.cere.ddc.core.extension.hexToBytes
import org.junit.jupiter.api.Test

internal class Secp256k1Test {

    private val signature =
        "0x789a80053e4927d0a898db8e065e948f5cf086e32f9ccaa54c1908e22ac430c62621578113ddbb62d509bf6049b8fb544ab06d36f916685a2eb8e57ffadde0231c"

    private val testSubject = Secp256k1("fad9c8855b740a0b7ed4c221dbad0f33a83a49cad6b3fe8d5817ac83d38b6a19".hexToBytes())

    @Test
    fun `Valid signature`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        result.shouldBeTrue()
    }

    @Test
    fun `Invalid signature`() {
        //given
        val message = "test"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        result.shouldBeFalse()
    }

    @Test
    fun `Sign data`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.sign(message.toByteArray())

        //then
        result shouldBe signature
    }
}