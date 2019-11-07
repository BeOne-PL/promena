package pl.beone.promena.transformer.internal.model.data.memory

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class MemoryWritableDataDslTest {

    @Test
    fun emptyMemoryWritableData_() {
        emptyMemoryWritableData().getBytes() shouldBe ByteArray(0)
    }
}