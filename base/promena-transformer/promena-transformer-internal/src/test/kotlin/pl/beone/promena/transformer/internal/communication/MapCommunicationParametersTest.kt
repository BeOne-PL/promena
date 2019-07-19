package pl.beone.promena.transformer.internal.communication

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.Test

class MapCommunicationParametersTest {

    companion object {
        private val communicationParameters = MapCommunicationParameters.builder()
                .parameter("id", "memory")
                .parameter("int", 3)
                .parameter("string", "value")
                .parameter("stringInt", "3")
                .parameter("location", "file:/tmp")
                .build()
    }

    @Test
    fun of() {
        MapCommunicationParameters.of("memory").getAll().size shouldBe 1
        MapCommunicationParameters.of("memory", mapOf("int" to 3)).getAll().size shouldBe 2
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
    fun getId() {
        communicationParameters.getId() shouldBe "memory"
    }

    @Test
    fun getAll() {
        communicationParameters.getAll().size shouldBe 5
        communicationParameters.getAll() shouldContainAll mapOf("location" to "file:/tmp")
    }
}