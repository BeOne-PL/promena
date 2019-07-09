package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import org.junit.Test

class MemoryDataTest {

    companion object {
        private val bytes = "test".toByteArray()
    }

    @Test
    fun getBytes() {
        MemoryData(bytes).getBytes() shouldBe bytes
    }

    @Test
    fun `getLocation _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> { MemoryData(bytes).getLocation() }
                .message shouldBe "This resource exists only in memory"
    }

    @Test
    fun isAvailable() {
        shouldNotThrowAny { MemoryData(bytes).isAccessible() }
    }

    @Test
    fun delete() {
        shouldThrow<UnsupportedOperationException> {
            MemoryData(bytes).delete()
        }.message shouldBe "This resource exists only in memory"
    }

    @Test
    fun equals() {
        MemoryData(bytes) shouldBe MemoryData("test".toByteArray())
    }
}