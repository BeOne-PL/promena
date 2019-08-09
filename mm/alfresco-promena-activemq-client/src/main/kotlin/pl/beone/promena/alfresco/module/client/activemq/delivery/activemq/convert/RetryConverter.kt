package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert

import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import java.time.Duration

class RetryConverter {

    fun convert(maxAttempts: Long, nextAttemptDelay: String): Retry =
        customRetry(maxAttempts, Duration.parse(nextAttemptDelay))
}