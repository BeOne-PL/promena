package pl.beone.promena.transformer.internal.communication

import io.kotlintest.shouldBe
import org.junit.Test
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

class MapCommunicationParametersDslTest {

    @Test
    fun communicationParameters() {
        communicationParameters("memory").getAll() shouldBe
                mapOf(CommunicationParameters.ID to "memory")

        communicationParameters("memory", mapOf("key" to "value")).getAll() shouldBe
                mapOf(CommunicationParameters.ID to "memory",
                      "key" to "value")
    }

    @Test
    fun plus() {
        (communicationParameters("memory") + ("key" to "value") + ("key2" to "value2")).getAll() shouldBe
                mapOf(CommunicationParameters.ID to "memory", "key" to "value", "key2" to "value2")
    }
}