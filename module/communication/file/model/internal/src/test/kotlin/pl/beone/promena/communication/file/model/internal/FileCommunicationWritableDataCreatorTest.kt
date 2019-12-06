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
        with(FileCommunicationWritableDataCreator.create(fileCommunicationParameters(directory))) {
            this shouldBe instanceOf(FileWritableData::class)
            getBytes() shouldBe ByteArray(0)
            getLocation().path shouldStartWith directory.path
        }
    }
}