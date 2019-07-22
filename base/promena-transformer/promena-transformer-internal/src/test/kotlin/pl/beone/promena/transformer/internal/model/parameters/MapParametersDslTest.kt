package pl.beone.promena.transformer.internal.model.parameters

import io.kotlintest.shouldBe
import org.junit.Test
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

class MapParametersDslTest {

    @Test
    fun emptyParameters_() {
        emptyParameters().getAll() shouldBe
                emptyMap()
    }

    @Test
    fun parameters() {
        parameters(mapOf("key" to "value")).getAll() shouldBe
                mapOf("key" to "value")
    }

    @Test
    fun `plus and addTimeout`() {
        (emptyParameters() + ("key" to "value") + ("key2" to "value2") addTimeout Duration.ofMillis(100)).getAll() shouldBe
                mapOf("key" to "value", "key2" to "value2", Parameters.TIMEOUT to Duration.ofMillis(100))
    }
}