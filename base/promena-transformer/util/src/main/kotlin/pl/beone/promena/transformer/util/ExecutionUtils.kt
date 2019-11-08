package pl.beone.promena.transformer.util

import kotlinx.coroutines.*
import java.time.Duration
import java.util.concurrent.TimeoutException

fun <T> execute(timeout: Duration? = null, dispatcher: CoroutineDispatcher? = null, toRun: suspend () -> T) {
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
}