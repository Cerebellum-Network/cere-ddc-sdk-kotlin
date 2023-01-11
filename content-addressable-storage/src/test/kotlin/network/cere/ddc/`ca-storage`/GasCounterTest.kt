package network.cere.ddc.`ca-storage`

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class GasCounterTest {
    val gasCounter = GasCounter()

    @Test
    fun `should commit gas amount and counts overall amount`() {
        gasCounter.push(1)
        gasCounter.push(2)
        gasCounter.push(3)
        val gasAndGasCommit = gasCounter.readUncommitted()
        gasAndGasCommit.first shouldBe 6

        gasCounter.push(10)
        val gasAndGasCommit2 = gasCounter.readUncommitted()
        gasAndGasCommit2.first shouldBe 10
    }

    @Test
    fun `should apply commits`() {
        runBlocking {
            gasCounter.push(1)
            gasCounter.push(2)
            gasCounter.push(3)

            delay(51)
            val gasAndGasCommit = gasCounter.readUncommitted()
            gasAndGasCommit.first shouldBe 6

            gasCounter.push(4)
            gasCounter.commit(gasAndGasCommit.second)

            delay(51)
            val gasAndGasCommit2 = gasCounter.readUncommitted()
            gasAndGasCommit2.first shouldBe 4
        }
    }

    @Test
    fun `should apply resets`() {
        runBlocking {
            gasCounter.push(1)
            gasCounter.push(2)
            gasCounter.push(3)

            delay(51)
            val gasAndGasCommit = gasCounter.readUncommitted()
            gasAndGasCommit.first shouldBe 6

            gasCounter.push(4)
            gasCounter.revert(gasAndGasCommit.second)

            delay(51)
            val gasAndGasCommit2 = gasCounter.readUncommitted()
            gasAndGasCommit2.first shouldBe 10
        }
    }


}
