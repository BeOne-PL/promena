package pl.beone.promena.alfresco.module.client.base.applicationmodel.retry

import io.kotlintest.shouldBe
import org.junit.Test

import org.junit.Assert.*
import java.time.Duration

class RetryDslTest {

    @Test
    fun noRetry_() {
        noRetry() shouldBe
                Retry.No
    }

    @Test
    fun customRetry() {
        val maxAttempts: Long = 5
        val nextAttemptDelay = Duration.ofMillis(1000)

        customRetry(maxAttempts, nextAttemptDelay) shouldBe
                Retry.Custom(maxAttempts, nextAttemptDelay)
    }
}