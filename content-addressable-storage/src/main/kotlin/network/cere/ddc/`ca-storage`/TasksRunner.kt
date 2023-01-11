package network.cere.ddc.`ca-storage`

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TasksRunner(private val timeout: Int) {
    private val queue = HashMap<suspend (Array<ByteArray>) -> Unit, Array<ByteArray>>()

    private val jobs = HashSet<Job>()

    private var job: Job? = null

    suspend fun addTask(task: suspend (Array<ByteArray>) -> Unit, vararg args: ByteArray) {
        if (!queue.containsKey(task)) {
            runTask(task)
        }
        args.map { it }.toTypedArray().let { queue.set(task, it) }
    }

    fun reset() {
        queue.clear()
        jobs.forEach {
            run {
                it.cancel()
            }
        }
        jobs.clear()
    }

    private suspend fun runTask(task: suspend (Array<ByteArray>) -> Unit) {
        CoroutineScope(SupervisorJob()).launch {
            job = launch {
                delay(timeout.toLong())
                val args: Array<ByteArray> = queue[task] ?: arrayOf()
                try {
                    task(args)
                    queue.remove(task)
                } catch (e: Exception) {
                    runTask(task)
                }
            }
            jobs.add(job!!)
        }
    }
}