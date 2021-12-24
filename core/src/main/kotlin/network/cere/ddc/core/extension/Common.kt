package network.cere.ddc.core.extension

import kotlinx.coroutines.time.delay
import java.time.Duration

suspend inline fun <reified R> retry(
    times: Int = 3,
    backOff: Duration = Duration.ZERO,
    predicate: (Exception) -> Boolean,
    action: () -> R
): R {
    repeat(times - 1) {
        try {
            return action()
        } catch (e: Exception) {
            if (predicate(e)) delay(backOff) else throw e
        }
    }

    return action()
}