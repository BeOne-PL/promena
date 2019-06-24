package pl.beone.promena.connector.http.delivery.http

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
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

        parameters.getAll().entries shouldHaveSize 8

        parameters.get("firstPages") shouldBe 2L
        parameters.get("page") shouldBe 4L
        parameters.get("onlyHeader") shouldBe true
        parameters.get("onlyScan") shouldBe false
        parameters.get("barcode") shouldBe "EAN"
        parameters.get("value") shouldBe 3.4
        parameters.get("stringValueLookingLikeIncorrectDouble") shouldBe "3.4.5.6.7.8.9"
        parameters.get("list") shouldBe listOf(1.0, 2.5)

        parameters.get("onlyScan", String::class.java) shouldBe "false"
        parameters.get("value", Float::class.java) shouldBe 3.4f
    }
}