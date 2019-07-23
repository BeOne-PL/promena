package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import org.junit.Test

class MemoryDataDslKtTest {

    companion object {
        private const val string = "bytes"
        private val bytes = string.toByteArray()
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