package pl.beone.promena.transformer.internal.model.data.file

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import java.io.File
import java.net.URI

class FileDataTest {

    companion object {
        private const val fileString = "test"
        private val fileBytes = fileString.toByteArray()
        private val file = fileString.createTmpFile()
    }

    @Test
    fun `of _ file`() {
        FileData.of(file).getBytes() shouldBe fileBytes
    }

    @Test
    fun `of _ non-existent file _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            FileData.of(File("/notExists")).getBytes()
        }.message shouldBe "File </notExists> doesn't exist or isn't file"
    }

    @Test
    fun `of _ file is directory _ should throw IllegalArgumentException`() {
        val directory = createTempDir()

        shouldThrow<IllegalArgumentException> {
            FileData.of(directory).getBytes()
        }.message shouldBe "File <$directory> doesn't exist or isn't file"
    }

    @Test
    fun `of _ input stream and directory`() {
        FileData.of(fileString.byteInputStream(), createTempDir()).getBytes() shouldBe fileBytes
    }

    @Test
    fun `of _ input stream and non-existent directory _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            FileData.of(fileString.byteInputStream(), File("/notExists")).getBytes()
        }.message shouldBe "Directory </notExists> doesn't exist or isn't directory"
    }

    @Test
    fun `of _ input stream and directory is file _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            FileData.of(fileString.byteInputStream(), file).getBytes()
        }.message shouldBe "Directory <$file> doesn't exist or isn't directory"
    }

    @Test
    fun `getBytes _ unreachable file _ should throw DataAccessibilityException`() {
        val data = FileData.of(createTempFile())
            .also { it.delete() }

        shouldThrow<DataAccessibilityException> {
            data.getBytes()
        }.message shouldBe "File <${data.getLocation().toFile()}> doesn't exist"
    }

    @Test
    fun getBytes() {
        FileData.of(file).getBytes() shouldBe fileBytes
    }

    @Test
    fun getInputStream() {
        FileData.of(file).getInputStream().readAllBytes() shouldBe fileBytes
    }

    @Test
    fun getLocation() {
        FileData.of(file).getLocation() shouldBe file.toURI()
    }

    @Test
    fun isAccessible() {
        shouldNotThrow<DataAccessibilityException> {
            FileData.of(file).isAccessible()
        }
    }

    @Test
    fun delete() {
        val data = FileData.of(createTempFile())
            .also { it.delete() }

        data.getLocation().toFile().exists() shouldBe false
    }

    @Test
    fun `delete _ should throw DataDeleteException`() {
        val data = FileData.of(createTempFile())
            .also { it.delete() }

        with(shouldThrow<DataDeleteException> {
            data.delete()
        }) {
            message shouldBe "Couldn't delete <${data.getLocation().toFile()}> file"
            cause!!.message shouldBe "File <${data.getLocation().toFile()}> wasn't successfully deleted. Maybe file doesn't exist"
        }
    }

    private fun URI.toFile(): File =
        File(this)
}
