package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import java.io.File
import java.io.IOException
import java.net.URI

class FileDataTest {

    companion object {
        private const val fileString = "test"
        private val fileBytes = fileString.toByteArray()
        private val fileUri = fileString.createTmpFile().toURI()

        private val notReachableFileUri = URI("file:/doesNotExist")
    }

    @Test
    fun `of _ uri`() {
        FileData.of(fileUri).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `of _ file`() {
        FileData.of(fileString.createTmpFile()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `of _ input stream and directory uri`() {
        val directory = createTempDir()

        FileData.of(fileString.createTmpFile(directory).inputStream(), directory.toURI()).getBytes() shouldBe
                fileBytes

        FileData.of(fileString.byteInputStream(), directory.toURI()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `of _ input stream and directory file`() {
        FileData.of(fileString.byteInputStream(), createTempDir()).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun `of _ input stream and not reachable directory uri _ should throw IOException`() {
        shouldThrow<IOException> {
            FileData.of(fileString.byteInputStream(), notReachableFileUri).getBytes()
        }.message shouldBe "URI <$notReachableFileUri> doesn't exist or isn't a directory"
    }

    @Test
    fun `of _ input stream and directory which is file uri _ should throw IOException`() {
        val fileUri = createTempFile().toURI()

        shouldThrow<IOException> {
            FileData.of(fileString.byteInputStream(), fileUri).getBytes()
        }.message shouldBe "URI <$fileUri> doesn't exist or isn't a directory"
    }

    @Test
    fun `init _ http scheme _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> { FileData.of(URI("http://noMatter.com/")) }.message shouldBe
                "Location URI <http://noMatter.com/> has <http> scheme but this implementation supports only <file> scheme"
    }

    @Test
    fun getBytes() {
        FileData.of(fileUri).getBytes() shouldBe
                fileBytes
    }

    @Test
    fun getInputStream() {
        FileData.of(fileUri).getInputStream().readAllBytes() shouldBe
                fileBytes
    }

    @Test
    fun `getInputStream _ unreachable file _ should throw ResourceIsNotReachableException`() {
        shouldThrow<DataAccessibilityException> { FileData.of(notReachableFileUri).getInputStream() shouldBe fileBytes }.message shouldBe
                "File <file:/doesNotExist> doesn't exist"
    }

    @Test
    fun `getBytes _ unreachable file _ should throw ResourceIsNotReachableException`() {
        shouldThrow<DataAccessibilityException> { FileData.of(notReachableFileUri).getBytes() }.message shouldBe
                "File <file:/doesNotExist> doesn't exist"
    }

    @Test
    fun getLocation() {
        FileData.of(fileUri).getLocation() shouldBe
                fileUri
    }

    @Test
    fun isAccessible() {
        shouldNotThrow<DataAccessibilityException> {
            FileData.of(fileUri).isAccessible()
        }
    }

    @Test
    fun delete() {
        val file = createTempFile().apply { writeText(fileString) }

        FileData.of(file.toURI()).delete()

        file.exists() shouldBe
                false
    }

    @Test
    fun `delete _ should throw DataDeleteException`() {
        val notExistFile = createTempDir().resolve(File(fileString)).toURI()

        shouldThrow<DataDeleteException> {
            FileData.of(notExistFile).delete()
        }.message shouldBe "Couldn't delete <$notExistFile> file. Maybe file doesn't exist"
    }

    @Test
    fun `isAvailable _ should throw DataAccessibilityException`() {
        shouldThrow<DataAccessibilityException> {
            FileData.of(notReachableFileUri).isAccessible()
        }.message shouldBe "File <file:/doesNotExist> doesn't exist"
    }
}
