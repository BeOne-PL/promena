package pl.beone.promena.lib.connector.http.extension

import reactor.core.publisher.Mono
import reactor.retry.Retry.allBut
import reactor.retry.Retry.any
import reactor.retry.RetryExhaustedException
import java.time.Duration

fun <T> Mono<T>.retryOnError(
    maxAttempts: Long,
    nextAttemptDelay: Duration,
    allButClass: Class<out Throwable>? = null,
    doOnRetry: (iteration: Long) -> Unit
): Mono<T> =
    retryWhen(
        (if (allButClass != null) allBut<T>(allButClass) else any())
            .fixedBackoff(nextAttemptDelay)
            .retryMax(maxAttempts)
            .doOnRetry { doOnRetry(it.iteration()) }
    )
        .onErrorMap(::unwrapRetryExhaustedException)

private fun unwrapRetryExhaustedException(exception: Throwable): Throwable =
    if (exception is RetryExhaustedException) exception.cause!! else exception