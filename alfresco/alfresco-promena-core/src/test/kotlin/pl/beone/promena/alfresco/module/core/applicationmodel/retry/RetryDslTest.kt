package pl.beone.promena.alfresco.module.core.applicationmodel.retry

import io.kotlintest.shouldBe
import org.junit.Test
import java.time.Duration

class RetryDslTest {

    @Test
    fun noRetry_() {
        noRetry() shouldBe
                Retry.No
    }

    @Test
    fun customRetry() {
        val maxAttempts = 5L
        val nextAttemptDelay = Duration.ofMillis(1000)

        customRetry(maxAttempts, nextAttemptDelay) shouldBe
                Retry.Custom(maxAttempts, nextAttemptDelay)
    }
}