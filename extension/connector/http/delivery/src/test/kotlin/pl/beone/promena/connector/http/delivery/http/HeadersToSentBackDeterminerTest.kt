package pl.beone.promena.connector.http.delivery.http

import io.kotlintest.matchers.maps.shouldContainExactly
import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap

class HeadersToSentBackDeterminerTest {

    @Test
    fun determine() {
        HeadersToSentBackDeterminer().determine(
            LinkedMultiValueMap(
                mapOf(
                    "key" to listOf("value"),
                    "send-back-key" to listOf("send-back-value")
                )
            )
        ) shouldContainExactly
                LinkedMultiValueMap(mapOf("send-back-key" to listOf("send-back-value")))
    }
}