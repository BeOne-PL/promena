package pl.beone.promena.transformer.internal.model.parameters

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.exception.general.ConversionException

class MapParametersTest {

    private val parameters = MapParameters(mapOf(
            "int" to 3,
            "long" to 10L,
            "double" to 3.5,
            "float" to 4.1f,
            "boolean" to true,
            "string" to "value",

            "stringInt" to "3",
            "stringLong" to "10",
            "stringDouble" to "3.5",
            "stringFloat" to "4.1",
            "stringBoolean" to "true",
            "stringBoolean2" to "false",

            "parameters" to MapParameters(mapOf("key" to "value")),
            "mapParameters" to mapOf("mapKey" to "mapValue"),

            "intList" to listOf(1, 2, 3),
            "mixList" to listOf(1, "string", true),
            "stringList" to listOf("1", "2", "3")
    ))

    @Test
    fun empty() {
        assertThat(MapParameters.empty().getAll()).hasSize(0)
    }

    @Test
    fun get() {
        assertThat(parameters.get("int")).isEqualTo(3)
        assertThat(parameters.get("long")).isEqualTo(10L)
        assertThat(parameters.get("double")).isEqualTo(3.5)
        assertThat(parameters.get("float")).isEqualTo(4.1f)
        assertThat(parameters.get("boolean")).isEqualTo(true)
        assertThat(parameters.get("string")).isEqualTo("value")

        assertThatThrownBy { parameters.get("absent") }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent> element")
    }

    @Test
    fun `get with class`() {
        assertThat(parameters.get("int", Int::class.java)).isEqualTo(3)
        assertThat(parameters.get("long", Long::class.java)).isEqualTo(10L)
        assertThat(parameters.get("double", Double::class.java)).isEqualTo(3.5)
        assertThat(parameters.get("float", Float::class.java)).isEqualTo(4.1f)
        assertThat(parameters.get("boolean", Boolean::class.java)).isEqualTo(true)
        assertThat(parameters.get("string", String::class.java)).isEqualTo("value")

        assertThat(parameters.get("stringInt", Int::class.java)).isEqualTo(3)
        assertThat(parameters.get("stringLong", Long::class.java)).isEqualTo(10L)
        assertThat(parameters.get("double", Double::class.java)).isEqualTo(3.5)
        assertThat(parameters.get("stringFloat", Float::class.java)).isEqualTo(4.1f)
        assertThat(parameters.get("stringBoolean", Boolean::class.java)).isEqualTo(true)
        assertThat(parameters.get("stringBoolean2", Boolean::class.java)).isEqualTo(false)

        assertThat(parameters.get("int", Long::class.java)).isEqualTo(3L)
        assertThat(parameters.get("stringInt", Int::class.java)).isEqualTo(3)

        assertThatThrownBy { parameters.get("string", Boolean::class.java) }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Couldn't convert <value> (java.lang.String) to <boolean>")

        assertThatThrownBy { parameters.get("stringBoolean", Long::class.java) }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Couldn't convert <true> (java.lang.String) to <long>")
        assertThatThrownBy { parameters.get("stringInt", IntRange::class.java) }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Converting from <java.lang.String> to <kotlin.ranges.IntRange> isn't supported")

        assertThatThrownBy { parameters.get("absent", String::class.java) }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent> element")
    }

    @Test
    fun getTimeout() {
        assertThat(MapParameters(mapOf("timeout" to 5000)).getTimeout())
                .isEqualTo(5000)

        assertThatThrownBy { MapParameters.empty().getTimeout() }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <timeout> element")
    }

    @Test
    fun getParameters() {
        assertThat(parameters.getParameters("parameters")).isEqualTo(MapParameters(mapOf("key" to "value")))

        assertThatThrownBy { parameters.getParameters("stringBoolean") }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Converting from <java.lang.String> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported")
        assertThatThrownBy { parameters.getParameters("mapParameters") }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Converting from <java.util.Collections\$SingletonMap> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported")

        assertThatThrownBy { parameters.getParameters("absent") }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent> element")
    }

    @Test
    fun getList() {
        assertThat(parameters.getList("intList")).isEqualTo(listOf(1, 2, 3))
        assertThat(parameters.getList("mixList")).isEqualTo(listOf(1, "string", true))
        assertThat(parameters.getList("stringList")).isEqualTo(listOf("1", "2", "3"))

        assertThatThrownBy { parameters.getList("stringBoolean") }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Converting from <java.lang.String> to <java.util.List> isn't supported")

        assertThatThrownBy { parameters.getList("absent") }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent> element")
    }

    @Test
    fun `getList with class`() {
        assertThat(parameters.getList("intList", Int::class.java)).isEqualTo(listOf(1, 2, 3))
        assertThat(parameters.getList("stringList", String::class.java)).isEqualTo(listOf("1", "2", "3"))
        assertThat(parameters.getList("stringList", Long::class.java)).isEqualTo(listOf(1L, 2L, 3L))

        assertThatThrownBy { parameters.getList("mixList", Int::class.java) }
                .isExactlyInstanceOf(ConversionException::class.java)
                .hasMessage("Couldn't convert <[1, string, true]> to List<int>")

        assertThatThrownBy { parameters.getList("absent") }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent> element")
    }

    @Test
    fun getAll() {
        assertThat(parameters.getAll())
                .hasSize(17)
                .containsEntry("int", 3)
                .containsEntry("int", 3)
                .containsEntry("boolean", true)
                .containsEntry("stringFloat", "4.1")
                .containsEntry("parameters", MapParameters(mapOf("key" to "value")))
                .containsEntry("mixList", listOf(1, "string", true))
    }
}