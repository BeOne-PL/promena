package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import org.junit.Test

class FileDataDslTest {

    companion object {
        private const val fileString = "test"
        private val fileBytes = fileString.toByteArray()
        private val fileUri = fileString.createTmpFile().toURI()
    }

    @Test
    fun `fileData _ uri`() {
        fileData(fileUri).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `fileData _ file`() {
        fileData(fileString.createTmpFile()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `fileData _ input stream and directory uri`() {
        FileData.of(fileString.byteInputStream(), createTempDir().toURI()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `fileData _ input stream and directory file`() {
        fileData(fileString.byteInputStream(), createTempDir()).getBytes() shouldBe
                fileBytes
    }
}