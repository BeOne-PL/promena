package pl.beone.promena.connector.messagebroker.delivery.jms

import io.kotlintest.matchers.maps.shouldContainExactly
import org.junit.Test

class CommunicationParametersConverterTest {

    companion object {
        private val communicationParametersConverter = CommunicationParametersConverter()
    }

    @Test
    fun convert() {
        communicationParametersConverter.convert(mapOf("key" to "value",
                                                                                                                                                            "promena_com_key" to "promena_com_value",
                                                                                                                                                            "promena_com_location" to "locationValue")).getAll() shouldContainExactly
                mapOf("key" to "promena_com_value",
                      "location" to "locationValue")
    }
}