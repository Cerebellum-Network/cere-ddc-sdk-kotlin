package network.cere.ddc.core.signature

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

internal class Sr25519Test {


    private val signature =
        "0x92d20f82f149976ac58c67425368cd45940418b770bd1c19b777057c10a6fc662bf250fc587df78bf0150dec9c3219c42dbeb5e531714ad2800814482ddac887"

    private val testSubject = Sr25519("0x500c89905aba00fc5211a2876b6105001ad8c77218b37b649ee38b4996e0716d6d5c7e03bbedbf39ca3588b5e6e0768e70a4507cd9e089f625929f9efae051f2")

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
        testSubject.verify(message.toByteArray(), result).shouldBeTrue()
    }
}