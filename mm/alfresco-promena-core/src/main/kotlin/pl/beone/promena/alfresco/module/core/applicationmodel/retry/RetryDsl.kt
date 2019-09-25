@file:JvmName("RetryDsl")

package pl.beone.promena.alfresco.module.core.applicationmodel.retry

import java.time.Duration

fun noRetry(): Retry.No =
    Retry.No

fun customRetry(maxAttempts: Long, nextAttemptDelay: Duration): Retry.Custom =
    Retry.Custom(maxAttempts, nextAttemptDelay)