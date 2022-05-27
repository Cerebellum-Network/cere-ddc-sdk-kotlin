package network.cere.ddc.core.signature

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import network.cere.ddc.core.extension.hexToBytes
import org.junit.jupiter.api.Test

internal class Sr25519Test {


    private val signature =
        "0xe6383e3460eeea83a4b5c4f82eae69aa7ece8f6616a06f1694cb5061cd94b04e4302c210740b4d83710d2d3ec224c975754e6de6c81e46b7a88ee84858636882"

    private val testSubject = Sr25519("2cf8a6819aa7f2a2e7a62ce8cf0dca2aca48d87b2001652de779f43fecbc5a03".hexToBytes())

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