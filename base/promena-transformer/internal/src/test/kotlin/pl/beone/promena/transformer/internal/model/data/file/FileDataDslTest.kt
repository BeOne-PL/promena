package pl.beone.promena.transformer.internal.model.data.file

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class FileDataDslTest {

    companion object {
        private const val fileString = "test"
        private val fileBytes = fileString.toByteArray()
    }

    @Test
    fun `fileData _ file`() {
        fileData(fileString.createTmpFile()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `fileData _ input stream and directory`() {
        fileData(fileString.byteInputStream(), createTempDir()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun getFile() {
        val file = fileString.createTmpFile()
        fileData(file).getFile() shouldBe
                file
    }
}