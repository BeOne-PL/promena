package pl.beone.promena.transformer.internal.model.metadata

import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

// The same parent as MapParameters. It is covered by MapParametersJavaTest and MapParametersTest tests
class MapMetadataTest {

    companion object {
        private val metadata = emptyMetadata() +
                ("int" to 3) +
                ("string" to "value") +

                ("stringInt" to "3") +

                ("metadata" to metadata(mapOf("key" to "value"))) +
                ("mapMetadata" to mapOf("mapKey" to "mapValue")) +

                ("intList" to listOf(1, 2, 3)) +
                ("stringList" to listOf("1", "2", "3"))
    }

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
    fun `getOrNull with class`() {
        metadata.getOrNull("absent", Int::class.java) shouldBe null
    }

    @Test
    fun `getOrDefault with class`() {
        metadata.getOrDefault("absent", Int::class.java, 5) shouldBe 5
    }

    @Test
    fun getMetadata() {
        metadata.getMetadata("metadata") shouldBe
                (emptyMetadata() + ("key" to "value"))
    }

    @Test
    fun getMetadataOrNull() {
        metadata.getMetadataOrNull("metadata") shouldBe
                emptyMetadata() + ("key" to "value")
        metadata.getMetadataOrNull("absent") shouldBe null
    }

    @Test
    fun getMetadataOrDefault() {
        val assertMetadata = emptyMetadata() + ("key" to "value")
        metadata.getMetadataOrDefault("metadata", emptyMetadata()) shouldBe assertMetadata
        metadata.getMetadataOrDefault("absent", assertMetadata) shouldBe assertMetadata
    }

    @Test
    fun getList() {
        metadata.getList("intList") shouldBe listOf(1, 2, 3)
    }

    @Test
    fun getListOrNull() {
        metadata.getListOrNull("intList") shouldBe listOf(1, 2, 3)
        metadata.getListOrNull("absent") shouldBe null
    }

    @Test
    fun getListOrDefault() {
        val list = listOf(1, 2, 3)
        metadata.getListOrDefault("intList", emptyList()) shouldBe list
        metadata.getListOrDefault("absent", list) shouldBe list
    }

    @Test
    fun `getList with class`() {
        metadata.getList("intList", Int::class.java) shouldBe listOf(1, 2, 3)
        metadata.getList("stringList", Long::class.java) shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun `getListOrNull with class`() {
        metadata.getListOrNull("intList", Int::class.java) shouldBe listOf(1, 2, 3)
        metadata.getListOrNull("absent") shouldBe null
    }

    @Test
    fun `getListOrDefault with class`() {
        val list = listOf(1, 2, 3)
        metadata.getListOrDefault("intList", Int::class.java, emptyList()) shouldBe list
        metadata.getListOrDefault("absent", list) shouldBe list
    }

    @Test
    fun getAll() {
        metadata.getAll().size shouldBe 7
        metadata.getAll() shouldContainAll
                mapOf(
                    "int" to 3,
                    "metadata" to emptyMetadata() + ("key" to "value")
                )
    }
}