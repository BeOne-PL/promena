package pl.beone.promena.transformer.internal.model.metadata

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MapMetadataDslTest {

    @Test
    fun emptyMetadata_() {
        emptyMetadata().getAll() shouldBe
                emptyMap()
    }

    @Test
    fun metadata() {
        metadata(mapOf("key" to "value")).getAll() shouldBe
                mapOf("key" to "value")
    }

    @Test
    fun `plus _ pair`() {
        (emptyMetadata() +
                ("key" to "value") +
                ("key2" to "value2"))
            .getAll() shouldBe
                mapOf("key" to "value", "key2" to "value2")
    }

    @Test
    fun addIfNotNull() {
        (emptyMetadata() addIfNotNull ("key" to null)).getAll() shouldBe
                emptyMap()

        (emptyMetadata() addIfNotNull ("key" to "value")).getAll() shouldBe
                mapOf("key" to "value")
    }

    @Test
    fun `plus _ metadata`() {
        (metadata(mapOf("key" to "value")) +
                metadata(mapOf("key2" to "value2"))).getAll() shouldBe
                mapOf("key" to "value", "key2" to "value2")
    }
}