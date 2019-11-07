package pl.beone.promena.transformer.internal.model.data.file

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class FileWritableDataDslTest {

    companion object {
        private val emptyByteArray = ByteArray(0)
    }

    @Test
    fun fileWritableDataFromEmptyFile() {
        fileWritableDataFromEmptyFile(createTempFile()).getBytes() shouldBe emptyByteArray
    }

    @Test
    fun fileWritableDataFromDirectory() {
        fileWritableDataFromDirectory(createTempDir()).getBytes() shouldBe emptyByteArray
    }

    @Test
    fun toFileWritableDataFromEmptyFile() {
        createTempFile().toFileWritableDataFromEmptyFile().getBytes() shouldBe emptyByteArray
    }

    @Test
    fun toFileWritableDataFromDirectory() {
        createTempDir().toFileWritableDataFromDirectory().getBytes() shouldBe emptyByteArray
    }
}