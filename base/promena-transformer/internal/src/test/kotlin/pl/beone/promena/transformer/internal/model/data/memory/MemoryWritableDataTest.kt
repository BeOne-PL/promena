package pl.beone.promena.transformer.internal.model.data.memory

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.internal.model.data.file.FileWritableData

class MemoryWritableDataTest {

    @Test
    fun emptyByteArray() {
        MemoryWritableData.empty().getBytes() shouldBe ByteArray(0)
    }

    @Test
    fun getOutputStream() {
        val data = FileWritableData.ofDirectory(createTempDir())
        data.getOutputStream().apply {
            write("t".toByteArray())
            write("e".toByteArray())
            write("s".toByteArray())
            write("t".toByteArray())
        }
        data.getBytes() shouldBe "test".toByteArray()
    }
}