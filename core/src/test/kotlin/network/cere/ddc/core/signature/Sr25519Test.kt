package network.cere.ddc.core.signature

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class Sr25519Test {


    private val signature =
        "0x8632e09c7027619282a2f96df9348f704355dd685a0f82d2637f96299062313d9f8dd46047119ff1fe9c27531e885f615570a950c1bc0f6743e8467e36cb738a"

    private val testSubject = Sr25519("9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60")

    @Test
    fun `Valid signature`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        Assertions.assertThat(result).isTrue
    }

    @Test
    fun `Invalid signature`() {
        //given
        val message = "test"

        //when
        val result = testSubject.verify(message.toByteArray(), signature)

        //then
        Assertions.assertThat(result).isFalse
    }

    @Test
    fun `Sign data`() {
        //given
        val message = "hello"

        //when
        val result = testSubject.sign(message.toByteArray())

        //then
        Assertions.assertThat(testSubject.verify(message.toByteArray(), result)).isTrue
    }
}