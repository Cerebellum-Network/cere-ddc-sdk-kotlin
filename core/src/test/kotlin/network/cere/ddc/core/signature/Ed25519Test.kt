package network.cere.ddc.core.signature

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class Ed25519Test {

    private val signature =
        "0x85f4460e723da28da7d20dad261d4e89737e0daf93b53954b638a1fc540eb33c620e9e49dba7c0ffd1b8c21ee66f0318e3f8ffa9dafd3c223fa645f9c8960a07"

    private val testSubject = Ed25519("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")

    @Test
    fun `Valid signature`() {
        //given
        val message = "test_string"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        result.shouldBeTrue()
    }

    @Test
    fun `Invalid signature`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        result.shouldBeFalse()
    }

    @Test
    fun `Sign data`() {
        //given
        val message = "test_string"

        //when
        val result = testSubject.sign(message.toByteArray())

        //then
        result shouldBe signature
    }
}