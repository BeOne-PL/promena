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
    fun `of _ inputStream`() {
        MemoryData.of(bytes.inputStream()).getBytes() shouldBe bytes
    }

    @Test
    fun getBytes() {
        MemoryData.of(bytes).getBytes() shouldBe bytes
    }

    @Test
    fun getInputStream() {
        MemoryData.of(bytes).getInputStream().readAllBytes() shouldBe bytes
    }

    @Test
    fun `getLocation _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> { MemoryData.of(bytes).getLocation() }
                .message shouldBe "This resource exists only in memory"
    }

    @Test
    fun isAvailable() {
        shouldNotThrowAny { MemoryData.of(bytes).isAccessible() }
    }

    @Test
    fun delete() {
        shouldThrow<UnsupportedOperationException> {
            MemoryData.of(bytes).delete()
        }.message shouldBe "This resource exists only in memory"
    }

    @Test
    fun equals() {
        MemoryData.of(bytes) shouldBe MemoryData.of("test".byteInputStream())
    }
}