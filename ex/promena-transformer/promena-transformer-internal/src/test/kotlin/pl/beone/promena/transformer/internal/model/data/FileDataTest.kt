package pl.beone.promena.transformer.internal.model.data

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import java.net.URI

class FileDataTest {

    companion object {
        private val fileUri = createTempFile().apply { writeText("test") }.toURI()

        private val notReachableFileUri = URI("file:/doesNotExist")
    }

    @Test
    fun init() {
        FileData(fileUri)
    }

    @Test
    fun `init _ http scheme _ should throw UnsupportedOperationException`() {
        assertThatThrownBy { FileData(URI("http://noMatter.com/")) }
                .isExactlyInstanceOf(UnsupportedOperationException::class.java)
                .hasMessage("Location URI <http://noMatter.com/> has <http> scheme but this implementation supports only <file> scheme")
    }

    @Test
    fun getBytes() {
        FileData(fileUri).let {
            assertThat(it.getBytes()).isEqualTo("test".toByteArray())
        }
    }

    @Test
    fun `getBytes _ unreachable file _ should throw ResourceIsNotReachableException`() {
        assertThatThrownBy { FileData(notReachableFileUri).getBytes() }
                .isExactlyInstanceOf(DataAccessibilityException::class.java)
                .hasMessage("File <file:/doesNotExist> doesn't exist")
    }

    @Test
    fun getLocation() {
        FileData(fileUri).let {
            assertThat(it.getLocation()).isEqualTo(fileUri)
        }
    }

    @Test
    fun isAvailable() {
        FileData(fileUri).isAvailable()
    }

    @Test
    fun `isAvailable should throw DataAccessibilityException`() {
        assertThatThrownBy { FileData(notReachableFileUri).isAvailable() }
                .isExactlyInstanceOf(DataAccessibilityException::class.java)
                .hasMessage("File <file:/doesNotExist> doesn't exist")
    }
}