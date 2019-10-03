package pl.beone.promena.alfresco.module.core.applicationmodel.retry

import java.time.Duration

sealed class Retry {

    object No : Retry() {

        override val maxAttempts: Long
            get() = throw throwUnsupportedException()
        override val nextAttemptDelay: Duration
            get() = throw throwUnsupportedException()

        private fun throwUnsupportedException(): UnsupportedOperationException =
            UnsupportedOperationException("You can't get this value of <Retry.No> policy")
    }

    data class Custom internal constructor(
        override val maxAttempts: Long,
        override val nextAttemptDelay: Duration
    ) : Retry()

    abstract val maxAttempts: Long
    abstract val nextAttemptDelay: Duration
}