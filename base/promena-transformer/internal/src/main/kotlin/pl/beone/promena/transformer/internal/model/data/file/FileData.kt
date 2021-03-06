package pl.beone.promena.transformer.internal.model.data.file

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import pl.beone.promena.transformer.contract.model.data.Data
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI

/**
 * The implementation based on [File].
 *
 * @property file indicates the resource
 *
 * @see FileDataDsl
 */
open class FileData internal constructor(
    protected val file: File
) : Data {

    companion object {
        /**
         * @throws IllegalArgumentException if the [file] doesn't exist or isn't a file
         */
        @JvmStatic
        fun of(file: File): FileData {
            require(file.exists() && file.isFile) { "File <$file> doesn't exist or isn't file" }

            return FileData(file)
        }

        /**
         * Creates a temporary file in [directory] and copies the data from [inputStream] to the file.
         *
         * @throws IllegalArgumentException if [directory] doesn't exist or isn't a directory
         */
        @JvmStatic
        fun of(inputStream: InputStream, directory: File): FileData {
            require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't directory" }

            return createTempFile(directory = directory)
                .also { it.outputStream().use { outputStream -> inputStream.use { inputStream -> inputStream.copyTo(outputStream) } } }
                .let(::FileData)
        }
    }

    override fun getBytes(): ByteArray {
        isAccessible()

        return try {
            file.readBytes()
        } catch (e: Exception) {
            throw DataReadException("Couldn't read bytes from <$file>", e)
        }
    }

    override fun getInputStream(): InputStream {
        isAccessible()

        return file.inputStream()
    }

    override fun getLocation(): URI =
        file.toURI()

    override fun isAccessible() {
        if (!file.exists()) {
            throw DataAccessibilityException("File <$file> doesn't exist")
        }
    }

    override fun delete() {
        try {
            if (!file.delete()) {
                throw IOException("File <$file> wasn't successfully deleted. Maybe file doesn't exist")
            }
        } catch (e: Exception) {
            throw DataDeleteException("Couldn't delete <$file> file", e)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileData) return false

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}