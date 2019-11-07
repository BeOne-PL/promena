package pl.beone.promena.transformer.internal.model.data.memory

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MemoryDataDslTest {

    companion object {
        private const val string = "bytes"
        private val bytes = string.toByteArray()
    }

    @Test
    fun `memoryData _ byteArray`() {
        memoryData(bytes).getBytes() shouldBe
                bytes
    }

    @Test
    fun `memoryData _ inputStream`() {
        memoryData(bytes.inputStream()).getBytes() shouldBe
                bytes
    }

    @Test
    fun `toMemoryData _ byteArray`() {
        bytes.toMemoryData().getBytes() shouldBe
                bytes
    }

    @Test
    fun `toMemoryData _ inputStream`() {
        bytes.inputStream().toMemoryData().getBytes() shouldBe
                bytes
    }

    @Test
    fun `toMemoryData _ string`() {
        string.toMemoryData().getBytes() shouldBe
                bytes
    }
}