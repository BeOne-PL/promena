package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import io.kotlintest.matchers.maps.shouldContainExactly
import org.junit.Test

import org.junit.Assert.*

class ParametersConverterTest {

    companion object {
        private val parametersConverter = ParametersConverter()
    }

    @Test
    fun convert() {
        parametersConverter.convert(mapOf("key" to "value"))
                .getAll() shouldContainExactly mapOf("key" to "value")
    }
}