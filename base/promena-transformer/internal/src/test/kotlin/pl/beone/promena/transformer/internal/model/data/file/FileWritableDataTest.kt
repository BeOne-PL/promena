package pl.beone.promena.transformer.internal.model.data.file

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

internal class FileWritableDataTest {

    companion object {
        private val emptyByteArray = ByteArray(0)
    }

    @Test
    fun ofEmptyFile() {
        FileWritableData.ofEmptyFile(createTempFile()).getBytes() shouldBe emptyByteArray
    }

    @Test
    fun `ofEmptyFile _ non-existent file _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            FileWritableData.ofEmptyFile(File("/notExists")).getBytes()
        }.message shouldBe "File </notExists> doesn't exist or isn't file"
    }

    @Test
    fun `ofEmptyFile _ file is directory _ should throw IllegalArgumentException`() {
        val directory = createTempDir()

        shouldThrow<IllegalArgumentException> {
            FileWritableData.ofEmptyFile(directory).getBytes()
        }.message shouldBe "File <$directory> doesn't exist or isn't file"
    }

    @Test
    fun ofDirectory() {
        val directory = createTempDir()
        with(FileWritableData.ofDirectory(directory)) {
            getBytes() shouldBe emptyByteArray
            getFile().path shouldStartWith directory.path
        }
    }

    @Test
    fun `ofDirectory _ non-existent directory _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            FileWritableData.ofDirectory(File("/notExists")).getBytes()
        }.message shouldBe "Directory </notExists> doesn't exist or isn't directory"
    }

    @Test
    fun `ofDirectory _ directory is file _ should throw IllegalArgumentException`() {
        val file = createTempFile()
        shouldThrow<IllegalArgumentException> {
            FileWritableData.ofDirectory(file).getBytes()
        }.message shouldBe "Directory <$file> doesn't exist or isn't directory"
    }

    @Test
    fun getOutputStream() {
        val data = FileWritableData.ofDirectory(createTempDir())
        data.getOutputStream().apply {
            write("t".toByteArray())
            write("e".toByteArray())
            write("s".toByteArray())
            write("t".toByteArray())
        }
        data.getBytes() shouldBe "test".toByteArray()
    }
}