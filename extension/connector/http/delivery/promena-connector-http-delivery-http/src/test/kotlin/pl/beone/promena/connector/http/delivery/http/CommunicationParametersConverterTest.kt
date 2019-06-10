package pl.beone.promena.connector.http.delivery.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.util.LinkedMultiValueMap

class CommunicationParametersConverterTest {

    companion object {
        private val communicationParametersConverter = CommunicationParametersConverter()
    }

    @Test
    fun convert() {
        val parameters = communicationParametersConverter.convert(LinkedMultiValueMap(mapOf(
                "firstPages" to listOf("2"),
                "page" to listOf("4"),
                "onlyHeader" to listOf("true"),
                "onlyScan" to listOf("false"),
                "barcode" to listOf("EAN"),
                "value" to listOf("3.4"),
                "stringValueLookingLikeIncorrectDouble" to listOf("3.4.5.6.7.8.9"),
                "list" to listOf("1.0", "2.5")
        )))

        assertThat(parameters.getAll()).hasSize(8)

        assertThat(parameters.get("firstPages")).isEqualTo(2L)
        assertThat(parameters.get("page")).isEqualTo(4L)
        assertThat(parameters.get("onlyHeader")).isEqualTo(true)
        assertThat(parameters.get("onlyScan")).isEqualTo(false)
        assertThat(parameters.get("barcode")).isEqualTo("EAN")
        assertThat(parameters.get("value")).isEqualTo(3.4)
        assertThat(parameters.get("stringValueLookingLikeIncorrectDouble")).isEqualTo("3.4.5.6.7.8.9")
        assertThat(parameters.get("list")).isEqualTo(listOf(1.0, 2.5))

        assertThat(parameters.get("onlyScan", String::class.java)).isEqualTo("false")
        assertThat(parameters.get("value", Float::class.java)).isEqualTo(3.4f)
    }
}