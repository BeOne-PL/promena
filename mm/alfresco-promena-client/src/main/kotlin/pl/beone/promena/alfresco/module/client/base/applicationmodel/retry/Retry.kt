package pl.beone.promena.alfresco.module.client.base.applicationmodel.retry

import java.time.Duration

sealed class Retry {

    object No : Retry() {

        override val maxAttempts: Long
            get() = throw UnsupportedOperationException("")
        override val nextAttemptDelay: Duration
            get() = throw UnsupportedOperationException("")
    }

    data class Custom internal constructor(override val maxAttempts: Long,
                                           override val nextAttemptDelay: Duration) : Retry()

    abstract val maxAttempts: Long
    abstract val nextAttemptDelay: Duration
}