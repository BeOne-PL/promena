package pl.beone.promena.transformer.internal.model.metadata

import io.kotlintest.shouldBe
import org.junit.Test

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
    fun plus() {
        (emptyMetadata() + ("key" to "value") + ("key2" to "value2")).getAll() shouldBe
                mapOf("key" to "value", "key2" to "value2")
    }
}