package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert

import io.kotlintest.shouldBe
import org.junit.Test
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import java.time.Duration

class RetryConverterTest {

    companion object {
        private val retryConverter = RetryConverter()
    }

    @Test
    fun `convert _ no retry`() {
        retryConverter.convert(false, 0, Duration.ZERO.toString()) shouldBe
                noRetry()
    }

    @Test
    fun `convert _ custom retry`() {
        val maxAttempts = 5L
        val duration = Duration.ofMillis(400)

        retryConverter.convert(true, maxAttempts, duration.toString()) shouldBe
                customRetry(maxAttempts, duration)
    }
}