package pl.beone.promena.connector.http.internal.communication

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FromSpringMvcParametersMapToCommunicationParametersConverterTest {

    companion object {
        private val communicationParametersConverter = FromSpringMvcParametersMapToCommunicationParametersConverter()
    }

    @Test
    fun convert() {
        val parameters = communicationParametersConverter.convert(mapOf(
                "firstPages" to "2",
                "page" to "4",
                "onlyHeader" to "true",
                "onlyScan" to "false",
                "barcode" to "EAN",
                "value" to "3.4",
                "stringValueLookingLikeIncorrectDouble" to "3.4.5.6.7.8.9"
        ))

        assertThat(parameters.get("firstPages")).isEqualTo(2L)
        assertThat(parameters.get("page")).isEqualTo(4L)
        assertThat(parameters.get("onlyHeader")).isEqualTo(true)
        assertThat(parameters.get("onlyScan")).isEqualTo(false)
        assertThat(parameters.get("barcode")).isEqualTo("EAN")
        assertThat(parameters.get("value")).isEqualTo(3.4)
        assertThat(parameters.get("stringValueLookingLikeIncorrectDouble")).isEqualTo("3.4.5.6.7.8.9")

        assertThat(parameters.get("onlyScan", String::class.java)).isEqualTo("false")
        assertThat(parameters.get("value", Float::class.java)).isEqualTo(3.4f)

        assertThat(parameters.getAll()).hasSize(7)
    }
}