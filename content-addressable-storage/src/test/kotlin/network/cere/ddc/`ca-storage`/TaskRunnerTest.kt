package network.cere.ddc.`ca-storage`

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TaskRunnerTest {
    val tasksRunner = TasksRunner(200)
    val arr : Array<ByteArray> = arrayOf(byteArrayOf(1, 1))
    @Test
    fun `should run task once within the period`() {
        runBlocking {
        val fn: suspend (Array<ByteArray>) -> Unit = { arr[0][0] = 2 }
        val fn2: suspend (Array<ByteArray>) -> Unit = {arr[0][1] = 4 }
        tasksRunner.addTask(fn)
        tasksRunner.addTask(fn)

        delay(10)
        tasksRunner.addTask(fn2)
        tasksRunner.addTask(fn2)
        arr[0][0] shouldBe 1
        arr[0][1] shouldBe 1

        delay(201)
        arr[0][0] shouldBe 2
        arr[0][1] shouldBe 4
        }
    }

    @Test
    fun `should prevent task running after reset`() {
        runBlocking {
            val fn: suspend (Array<ByteArray>) -> Unit = { arr[0][0] = 2 }
            val fn2: suspend (Array<ByteArray>) -> Unit = {arr[0][1] = 4 }
            tasksRunner.addTask(fn)
            tasksRunner.addTask(fn2)

            delay(10)
            tasksRunner.reset()

            delay(220)
            arr[0][0] shouldBe 1
            arr[0][1] shouldBe 1
        }
    }

    @Test
    fun `should run scheduled task with latest arguments`() {
        runBlocking {
            val fn: suspend (Array<ByteArray>) -> Unit = { arr[0] = it[0] }
            tasksRunner.addTask(fn, "1".encodeToByteArray())

            delay(10)
            tasksRunner.addTask(fn, "2".encodeToByteArray())
            delay(220)

            arr[0] shouldBe "2".encodeToByteArray()
        }
    }
}