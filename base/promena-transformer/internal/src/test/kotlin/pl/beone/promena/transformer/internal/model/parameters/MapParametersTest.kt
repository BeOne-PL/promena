package pl.beone.promena.transformer.internal.model.parameters

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.promena.transformer.contract.model.Parameters.Companion.TIMEOUT
import java.time.Duration

class MapParametersTest {

    companion object {
        private val parameters = emptyParameters() +
                ("int" to 3) +
                ("long" to 10L) +
                ("double" to 3.5) +
                ("float" to 4.1f) +
                ("boolean" to true) +
                ("string" to "value") +
                ("char" to '$') +

                ("stringInt" to "3") +
                ("stringLong" to "10") +
                ("stringDouble" to "3.5") +
                ("stringFloat" to "4.1") +
                ("stringBoolean" to "true") +
                ("stringBoolean2" to "false") +

                ("parameter" to emptyParameters() + ("key" to "value")) +
                ("mapParameters" to mapOf("mapKey" to "value")) +

                ("intList" to listOf(1, 2, 3)) +
                ("mixList" to listOf(1, "string", true)) +
                ("stringList" to listOf("1", "2", "3"))

        private const val absentNoSuchElementExceptionMessage = "There is no <absent> element"
    }

    @Test
    fun empty() {
        MapParameters.empty().getAll() shouldBe
                emptyMap()
    }

    @Test
    fun `of _ timeout not specified`() {
        val parameters = mapOf("test" to "value")
        MapParameters.of(parameters).getAll() shouldBe
                parameters
    }

    @Test
    fun `of _ timeout specified`() {
        val duration = Duration.ofSeconds(3)
        MapParameters.of(mapOf("test" to "value"), duration).getAll() shouldBe
                mapOf("test" to "value", TIMEOUT to duration)
    }

    @Test
    fun get() {
        parameters.get("int") shouldBe 3
        parameters.get("long") shouldBe 10L
        parameters.get("double") shouldBe 3.5
        parameters.get("float") shouldBe 4.1f
        parameters.get("boolean") shouldBe true
        parameters.get("string") shouldBe "value"
        parameters.get("char") shouldBe '$'

        shouldThrow<NoSuchElementException> {
            parameters.get("absent")
        }.message shouldBe absentNoSuchElementExceptionMessage
    }

    @Test
    fun `get with class`() {
        parameters.get("int", Int::class.java) shouldBe 3
        parameters.get("long", Long::class.java) shouldBe 10L
        parameters.get("double", Double::class.java) shouldBe 3.5
        parameters.get("float", Float::class.java) shouldBe 4.1f
        parameters.get("boolean", Boolean::class.java) shouldBe true
        parameters.get("string", String::class.java) shouldBe "value"
        parameters.get("char", Char::class.java) shouldBe '$'

        parameters.get("stringInt", Int::class.java) shouldBe 3
        parameters.get("stringLong", Long::class.java) shouldBe 10L
        parameters.get("double", Double::class.java) shouldBe 3.5
        parameters.get("stringFloat", Float::class.java) shouldBe 4.1f
        parameters.get("stringBoolean", Boolean::class.java) shouldBe true
        parameters.get("stringBoolean2", Boolean::class.java) shouldBe false

        parameters.get("int", Long::class.java) shouldBe 3L
        parameters.get("stringInt", Int::class.java) shouldBe 3

        shouldThrow<TypeConversionException> {
            parameters.get("string", Boolean::class.java)
        }.message shouldBe "Couldn't convert <value> (java.lang.String) to <boolean>"

        shouldThrow<TypeConversionException> {
            parameters.get("stringBoolean", Long::class.java)
        }.message shouldBe "Couldn't convert <true> (java.lang.String) to <long>"
        shouldThrow<TypeConversionException> {
            parameters.get("stringInt", IntRange::class.java)
        }.message shouldBe "Converting from <java.lang.String> to <kotlin.ranges.IntRange> isn't supported"

        shouldThrow<NoSuchElementException> {
            parameters.get("absent", String::class.java)
        }.message shouldBe absentNoSuchElementExceptionMessage
    }

    @Test
    fun `getOrNull with class`() {
        parameters.getOrNull("absent", Int::class.java) shouldBe null
    }

    @Test
    fun `getOrDefault with class`() {
        parameters.getOrDefault("absent", Int::class.java, 5) shouldBe 5
    }

    @Test
    fun getTimeout() {
        val timeout = Duration.ofMillis(10)
        (emptyParameters() addTimeout timeout).getTimeout() shouldBe timeout

        shouldThrow<NoSuchElementException> {
            emptyParameters().getTimeout()
        }.message shouldBe "There is no <$TIMEOUT> element"
    }

    @Test
    fun getTimeoutOrNull() {
        val timeout = Duration.ofMillis(10)
        (emptyParameters() addTimeout timeout).getTimeoutOrNull() shouldBe timeout
        (emptyParameters()).getTimeoutOrNull() shouldBe null
    }

    @Test
    fun getTimeoutOrDefault() {
        val timeout = Duration.ofMillis(10)
        (emptyParameters() addTimeout timeout).getTimeoutOrDefault(Duration.ofSeconds(1)) shouldBe timeout
        (emptyParameters()).getTimeoutOrDefault(timeout) shouldBe timeout
    }

    @Test
    fun getParameters() {
        parameters.getParameters("parameter") shouldBe
                emptyParameters() + ("key" to "value")

        shouldThrow<TypeConversionException> {
            parameters.getParameters("stringBoolean")
        }.message shouldBe "Converting from <java.lang.String> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported"
        shouldThrow<TypeConversionException> {
            parameters.getParameters("mapParameters")
        }.message shouldBe "Converting from <java.util.Collections\$SingletonMap> to <pl.beone.promena.transformer.contract.model.Parameters> isn't supported"

        shouldThrow<NoSuchElementException> {
            parameters.getParameters("absent")
        }.message shouldBe absentNoSuchElementExceptionMessage
    }

    @Test
    fun getParametersOrNull() {
        parameters.getParametersOrNull("parameter") shouldBe
                emptyParameters() + ("key" to "value")
        parameters.getParametersOrNull("absent") shouldBe null
    }

    @Test
    fun getParametersOrDefault() {
        val assertParameters = emptyParameters() + ("key" to "value")
        parameters.getParametersOrDefault("parameter", emptyParameters()) shouldBe assertParameters
        parameters.getParametersOrDefault("absent", assertParameters) shouldBe assertParameters
    }

    @Test
    fun getList() {
        parameters.getList("intList") shouldBe listOf(1, 2, 3)
        parameters.getList("mixList") shouldBe listOf(1, "string", true)
        parameters.getList("stringList") shouldBe listOf("1", "2", "3")

        shouldThrow<TypeConversionException> {
            parameters.getList("stringBoolean")
        }.message shouldBe "Converting from <java.lang.String> to <java.util.List> isn't supported"

        shouldThrow<NoSuchElementException> {
            parameters.getList("absent")
        }.message shouldBe absentNoSuchElementExceptionMessage
    }

    @Test
    fun getListOrNull() {
        parameters.getListOrNull("intList") shouldBe listOf(1, 2, 3)
        parameters.getListOrNull("absent") shouldBe null
    }

    @Test
    fun getListOrDefault() {
        val list = listOf(1, 2, 3)
        parameters.getListOrDefault("intList", emptyList()) shouldBe list
        parameters.getListOrDefault("absent", list) shouldBe list
    }

    @Test
    fun `getList with class`() {
        parameters.getList("intList", Int::class.java) shouldBe listOf(1, 2, 3)
        parameters.getList("stringList", String::class.java) shouldBe listOf("1", "2", "3")
        parameters.getList("stringList", Long::class.java) shouldBe listOf(1L, 2L, 3L)

        shouldThrow<TypeConversionException> {
            parameters.getList("mixList", Int::class.java)
        }.message shouldBe "Couldn't convert <[1, string, true]> to List<int>"

        shouldThrow<NoSuchElementException> {
            parameters.getList("absent")
        }.message shouldBe absentNoSuchElementExceptionMessage
    }

    @Test
    fun `getListOrNull with class`() {
        parameters.getListOrNull("intList", Int::class.java) shouldBe listOf(1, 2, 3)
        parameters.getListOrNull("absent") shouldBe null
    }

    @Test
    fun `getListOrDefault with class`() {
        val list = listOf(1, 2, 3)
        parameters.getListOrDefault("intList", Int::class.java, emptyList()) shouldBe list
        parameters.getListOrDefault("absent", list) shouldBe list
    }

    @Test
    fun getAll() {
        parameters.getAll().size shouldBe 18
        parameters.getAll() shouldContainAll
                mapOf(
                    "int" to 3,
                    "boolean" to true,
                    "stringFloat" to "4.1",
                    "parameter" to emptyParameters() + ("key" to "value"),
                    "mixList" to listOf(1, "string", true)
                )
    }
}