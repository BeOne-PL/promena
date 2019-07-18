package pl.beone.promena.transformer.internal.model.metadata

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.Test

class MapMetadataTest {

    private val metadata = MapMetadata.builder()
            .metadata("int", 3)
            .metadata("string", "value")

            .metadata("stringInt", "3")

            .metadata("metadata", MapMetadata.of(mapOf("key" to "value")))
            .metadata("mapMetadata", mapOf("mapKey" to "mapValue"))

            .metadata("intList", listOf(1, 2, 3))
            .metadata("stringList", listOf("1", "2", "3"))
            .build()


    @Test
    fun empty() {
        MapMetadata.empty().getAll().size shouldBe 0
    }

    @Test
    fun of() {
        val metadata = mapOf("test" to "value")
        MapMetadata.of(metadata).getAll() shouldBe metadata
    }

    @Test
    fun get() {
        metadata.get("int") shouldBe 3
        metadata.get("string") shouldBe "value"
    }

    @Test
    fun `get with class`() {
        metadata.get("int", Int::class.java) shouldBe 3
        metadata.get("string", String::class.java) shouldBe "value"

        metadata.get("stringInt", Int::class.java) shouldBe 3
    }

    @Test
    fun getMetadata() {
        metadata.getMetadata("metadata") shouldBe (metadata() add ("key" to "value"))
    }

    @Test
    fun getList() {
        metadata.getList("intList") shouldBe listOf(1, 2, 3)
    }

    @Test
    fun `getList with class`() {
        metadata.getList("intList", Int::class.java) shouldBe listOf(1, 2, 3)
        metadata.getList("stringList", Long::class.java) shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun getAll() {
        metadata.getAll().size shouldBe 7
        metadata.getAll() shouldContainAll mapOf("int" to 3,
                                                 "metadata" to MapMetadata(mapOf("key" to "value")))
    }
}