package pl.beone.promena.core.internal.communication

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.Test
import java.net.URI

class MapCommunicationParametersTest {

    companion object {
        private val communicationParameters = MapCommunicationParameters(mapOf(
                "int" to 3,
                "string" to "value",
                "stringInt" to "3",
                "location" to "file:/tmp"
        ))
    }

    @Test
    fun empty() {
        MapCommunicationParameters.empty().getAll().size shouldBe 0
    }

    @Test
    fun get() {
        communicationParameters.get("int") shouldBe 3
        communicationParameters.get("string") shouldBe "value"
    }

    @Test
    fun `get with class`() {
        communicationParameters.get("int", Int::class.java) shouldBe 3
        communicationParameters.get("string", String::class.java) shouldBe "value"

        communicationParameters.get("stringInt", Int::class.java) shouldBe 3
    }

    @Test
    fun getLocation() {
        communicationParameters.getLocation() shouldBe URI("file:/tmp")
    }

    @Test
    fun getAll() {
        communicationParameters.getAll().size shouldBe 4
        communicationParameters.getAll() shouldContainAll mapOf("location" to "file:/tmp")
    }
}