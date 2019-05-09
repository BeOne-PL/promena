package pl.beone.promena.core.internal.communication

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URI

class MapCommunicationParametersTest {

    private val communicationParameters = MapCommunicationParameters(mapOf(
            "int" to 3,
            "string" to "value",

            "stringInt" to "3",

            "location" to "file:/tmp"
    ))

    @Test
    fun empty() {
        assertThat(MapCommunicationParameters.empty().getAll()).hasSize(0)
    }

    @Test
    fun get() {
        assertThat(communicationParameters.get("int")).isEqualTo(3)
        assertThat(communicationParameters.get("string")).isEqualTo("value")
    }

    @Test
    fun `get with class`() {
        assertThat(communicationParameters.get("int", Int::class.java)).isEqualTo(3)
        assertThat(communicationParameters.get("string", String::class.java)).isEqualTo("value")

        assertThat(communicationParameters.get("stringInt", Int::class.java)).isEqualTo(3)
    }

    @Test
    fun getLocation() {
        assertThat(communicationParameters.getLocation()).isEqualTo(URI("file:/tmp"))
    }

    @Test
    fun getAll() {
        assertThat(communicationParameters.getAll())
                .hasSize(4)
                .containsEntry("location", "file:/tmp")
    }
}