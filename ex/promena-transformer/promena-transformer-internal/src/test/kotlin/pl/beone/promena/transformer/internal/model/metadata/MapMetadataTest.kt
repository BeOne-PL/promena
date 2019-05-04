package pl.beone.promena.transformer.internal.model.metadata

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapMetadataTest {

    private val metadata = MapMetadata(mapOf(
            "int" to 3,
            "string" to "value",

            "stringInt" to "3",

            "metadata" to MapMetadata(mapOf("key" to "value")),
            "mapMetadata" to mapOf("mapKey" to "mapValue"),

            "intList" to listOf(1, 2, 3),
            "stringList" to listOf("1", "2", "3")
    ))

    @Test
    fun empty() {
        assertThat(MapMetadata.empty().getAll()).hasSize(0)
    }

    @Test
    fun get() {
        assertThat(metadata.get("int")).isEqualTo(3)
        assertThat(metadata.get("string")).isEqualTo("value")
    }

    @Test
    fun `get with class`() {
        assertThat(metadata.get("int", Int::class.java)).isEqualTo(3)
        assertThat(metadata.get("string", String::class.java)).isEqualTo("value")

        assertThat(metadata.get("stringInt", Int::class.java)).isEqualTo(3)
    }

    @Test
    fun getMetadata() {
        assertThat(metadata.getMetadata("metadata")).isEqualTo(MapMetadata(mapOf("key" to "value")))
    }

    @Test
    fun getList() {
        assertThat(metadata.getList("intList")).isEqualTo(listOf(1, 2, 3))
    }

    @Test
    fun `getList with class`() {
        assertThat(metadata.getList("intList", Int::class.java)).isEqualTo(listOf(1, 2, 3))
        assertThat(metadata.getList("stringList", Long::class.java)).isEqualTo(listOf(1L, 2L, 3L))
    }

    @Test
    fun getAll() {
        assertThat(metadata.getAll())
                .hasSize(7)
                .containsEntry("int", 3)
                .containsEntry("metadata", MapMetadata(mapOf("key" to "value")))
    }
}