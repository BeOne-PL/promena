package pl.beone.promena.transformer.util

import kotlinx.coroutines.*
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * Executes [toRun] on [dispatcher] and waits [timeout] for a result of the execution. If [timeout] is `null`, the execution has no time limit.
 *
 * @throws TimeoutException if [timeout] (if it is present) or default timeout is exceeded
 */
fun <T> execute(timeout: Duration? = null, dispatcher: CoroutineDispatcher? = null, toRun: suspend () -> T): T =
    try {
        runBlocking {
            withTimeout(timeout?.toMillis() ?: Long.MAX_VALUE) {
                if (dispatcher == null) {
                    toRun()
                } else {
                    withContext(dispatcher) {
                        toRun()
                    }
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        throw TimeoutException()
    }