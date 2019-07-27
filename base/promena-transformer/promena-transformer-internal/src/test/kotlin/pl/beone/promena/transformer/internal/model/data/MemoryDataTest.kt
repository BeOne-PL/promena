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
    fun `of _ byteArray`() {
        MemoryData.of(bytes).getBytes() shouldBe
                bytes
    }

    @Test
    fun `of _ inputStream`() {
        MemoryData.of(bytes.inputStream()).getBytes() shouldBe
                bytes
    }

    @Test
    fun getBytes() {
        bytes.toMemoryData().getBytes() shouldBe
                bytes
    }

    @Test
    fun getInputStream() {
        bytes.toMemoryData().getInputStream().readAllBytes() shouldBe
                bytes
    }

    @Test
    fun `getLocation _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> {
            bytes.toMemoryData().getLocation()
        }.message shouldBe "This resource exists only in memory"
    }

    @Test
    fun isAvailable() {
        shouldNotThrowAny {
            bytes.toMemoryData().isAccessible()
        }
    }

    @Test
    fun delete() {
        shouldThrow<UnsupportedOperationException> {
            bytes.toMemoryData().delete()
        }.message shouldBe "This resource exists only in memory"
    }

    @Test
    fun equals() {
        bytes.toMemoryData() shouldBe
                "test".byteInputStream().toMemoryData()
    }
}