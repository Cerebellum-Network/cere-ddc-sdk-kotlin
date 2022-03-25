package network.cere.ddc.storage.file

import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AsynchronousFileHandler : CompletionHandler<Int, Continuation<Int>> {
    override fun completed(bytesRead: Int, continuation: Continuation<Int>) {
        continuation.resume(bytesRead)
    }

    override fun failed(exception: Throwable, continuation: Continuation<Int>) {
        continuation.resumeWithException(exception)
    }
}