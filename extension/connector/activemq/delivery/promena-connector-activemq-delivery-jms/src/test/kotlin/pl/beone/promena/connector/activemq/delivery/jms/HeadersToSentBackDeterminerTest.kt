package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.maps.shouldContainExactly
import org.junit.Test

class HeadersToSentBackDeterminerTest {

    companion object {
        private val headersToSentBackDeterminer = HeadersToSentBackDeterminer()
    }

    @Test
    fun determine() {
        headersToSentBackDeterminer.determine(mapOf("key" to "value",
                                                    "send_back_key" to "send_back_value")) shouldContainExactly
                mapOf("send_back_key" to "send_back_value")
    }
}