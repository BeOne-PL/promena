package pl.beone.promena.transformer.internal.communication

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

// The same parent as MapParameters. It is covered by MapParametersJavaTest and MapParametersTest tests
class MapCommunicationParametersTest {

    companion object {
        private val communicationParameters = communicationParameters("memory") +
                ("int" to 3) +
                ("string" to "value") +
                ("stringInt" to "3") +
                ("directoryPath" to "/tmp") +
                ("directory" to File("/tmp"))
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
        communicationParameters.getAll().size shouldBe 6
        communicationParameters.getAll() shouldContainAll
                mapOf("directoryPath" to "/tmp", "directory" to File("/tmp"))
    }
}