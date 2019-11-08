package pl.beone.promena.communication.file.model.internal

import io.kotlintest.matchers.instanceOf
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.internal.model.data.file.FileWritableData

class FileCommunicationWritableDataCreatorTest {

    @Test
    fun create() {
        val directory = createTempDir()
        FileCommunicationWritableDataCreator.create(fileCommunicationParameters(directory)).let {
            it shouldBe instanceOf(FileWritableData::class)
            it.getBytes() shouldBe ByteArray(0)
            it.getLocation().path shouldStartWith directory.path
        }
    }
}