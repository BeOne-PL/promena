package pl.beone.promena.communication.memory.model.internal

import io.kotlintest.matchers.instanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.internal.model.data.memory.MemoryWritableData

class MemoryCommunicationWritableDataCreatorTest {

    @Test
    fun create() {
        with(MemoryCommunicationWritableDataCreator.create(memoryCommunicationParameters())) {
            this shouldBe instanceOf(MemoryWritableData::class)
            getBytes() shouldBe ByteArray(0)
        }
    }
}