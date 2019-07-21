package pl.beone.promena.connector.activemq.delivery.jms

import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

class CommunicationParametersConverterTest {

    companion object {
        private val communicationParametersConverter = CommunicationParametersConverter()
    }

    @Test
    fun convert() {
        communicationParametersConverter.convert(mapOf("key" to "value",
                                                       PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID to "memory",
                                                       "promena_communication_parameter_key" to "promena_communication_parameter_value",
                                                       "promena_communication_parameter_location" to "locationValue"))
                .getAll() shouldContainExactly mapOf(CommunicationParameters.ID to "memory",
                                                     "key" to "promena_communication_parameter_value",
                                                     "location" to "locationValue")
    }

    @Test
    fun `convert _ no id _ should throw NoSuchElementException`() {
        shouldThrow<NoSuchElementException> {
            communicationParametersConverter.convert(emptyMap())
        }.message shouldBe "Headers must contain at least <promena_communication_parameter_id> communication parameter"
    }
}