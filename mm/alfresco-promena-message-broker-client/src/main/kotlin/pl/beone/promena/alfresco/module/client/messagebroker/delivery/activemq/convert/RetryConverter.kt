package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import java.time.Duration

class RetryConverter {

    fun convert(
        enabled: Boolean,
        maxAttempts: Long,
        nextAttemptDelay: String
    ): Retry =
        if (enabled) {
            customRetry(maxAttempts, Duration.parse(nextAttemptDelay))
        } else {
            noRetry()
        }
}