package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import io.kotlintest.shouldBe
import org.junit.Test
import java.time.LocalDateTime

class TimestampConverterTest {

    companion object {
        private val timestampConverter = TimestampConverter()
    }

    @Test
    fun convert() {
        timestampConverter.convert(1559901383000) shouldBe LocalDateTime.of(2019, 6, 7, 11, 56, 23)
    }
}