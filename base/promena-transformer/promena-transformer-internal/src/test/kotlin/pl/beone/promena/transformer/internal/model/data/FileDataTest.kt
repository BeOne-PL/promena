package pl.beone.promena.transformer.internal.model.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import java.io.File
import java.net.URI

class FileDataTest {

    companion object {
        private val fileUri = createTempFile().apply { writeText("test") }.toURI()

        private val notReachableFileUri = URI("file:/doesNotExist")
    }

    @Test
    fun init() {
        FileData.of(fileUri)
    }

    @Test
    fun `init _ http scheme _ should throw UnsupportedOperationException`() {
        shouldThrow<UnsupportedOperationException> { FileData.of(URI("http://noMatter.com/")) }
                .message shouldBe "Location URI <http://noMatter.com/> has <http> scheme but this implementation supports only <file> scheme"
    }

    @Test
    fun getBytes() {
        FileData.of(fileUri).getBytes() shouldBe "test".toByteArray()
    }

    @Test
    fun getInputStream() {
        FileData.of(fileUri).getInputStream().readAllBytes() shouldBe "test".toByteArray()
    }

    @Test
    fun `getInputStream _ unreachable file _ should throw ResourceIsNotReachableException`() {
        shouldThrow<DataAccessibilityException> { FileData.of(notReachableFileUri).getInputStream() shouldBe "test".toByteArray() }
                .message shouldBe "File <file:/doesNotExist> doesn't exist"
    }

    @Test
    fun `getBytes _ unreachable file _ should throw ResourceIsNotReachableException`() {
        shouldThrow<DataAccessibilityException> { FileData.of(notReachableFileUri).getBytes() }
                .message shouldBe "File <file:/doesNotExist> doesn't exist"
    }

    @Test
    fun getLocation() {
        FileData.of(fileUri).getLocation() shouldBe fileUri
    }

    @Test
    fun isAccessible() {
        shouldNotThrow<DataAccessibilityException> {
            FileData.of(fileUri).isAccessible()
        }
    }

    @Test
    fun delete() {
        val file = createTempFile().apply { writeText("test") }

        FileData.of(file.toURI()).delete()

        file.exists() shouldBe false
    }

    @Test
    fun `delete _ should throw DataDeleteException`() {
        val notExistFile = createTempDir().resolve(File("test")).toURI()

        shouldThrow<DataDeleteException> {
            FileData.of(notExistFile).delete()
        }.message shouldBe "Couldn't delete <$notExistFile> file. Maybe file doesn't exist"
    }

    @Test
    fun `isAvailable _ should throw DataAccessibilityException`() {
        shouldThrow<DataAccessibilityException> { FileData.of(notReachableFileUri).isAccessible() }
                .message shouldBe "File <file:/doesNotExist> doesn't exist"
    }
}