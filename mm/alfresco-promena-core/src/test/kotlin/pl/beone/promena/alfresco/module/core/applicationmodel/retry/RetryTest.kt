package pl.beone.promena.alfresco.module.core.applicationmodel.retry

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import java.time.Duration

class RetryTest {

    @Test
    fun `getMaxAttempts and nextAttemptDelay _ no retry`() {
        val noRetry = noRetry()
        val exceptionMessage = "You can't get this value of <Retry.No> policy"

        shouldThrow<UnsupportedOperationException> {
            noRetry.maxAttempts
        }.message shouldBe exceptionMessage

        shouldThrow<UnsupportedOperationException> {
            noRetry.nextAttemptDelay
        }.message shouldBe exceptionMessage
    }

    @Test
    fun `getMaxAttempts and nextAttemptDelay _ custom retry`() {
        val maxAttempts = 5L
        val nextAttemptDelay = Duration.ofMillis(100)

        val customRetry = customRetry(maxAttempts, nextAttemptDelay)

        customRetry.maxAttempts shouldBe
                maxAttempts
        customRetry.nextAttemptDelay shouldBe
                nextAttemptDelay
    }

}