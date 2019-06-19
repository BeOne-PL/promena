package pl.beone.promena.connector.messagebroker.delivery.jms

import io.kotlintest.matchers.maps.shouldContainExactly
import org.junit.Test

class HeadersToSentBackDeterminerTest {

    @Test
    fun determine() {
        HeadersToSentBackDeterminer().determine(mapOf("key" to "value",
                                                    "send_back_key" to "send_back_value")) shouldContainExactly mapOf("send_back_key" to "send_back_value")
    }
}