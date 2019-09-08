package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import pl.beone.promena.transformer.contract.model.Data
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI

data class FileData internal constructor(
    private val file: File
) : Data {

    companion object {
        @JvmStatic
        fun of(file: File): FileData {
            require(file.exists() && file.isFile) { "File <$file> doesn't exist or isn't a file" }

            return FileData(file)
        }

        @JvmStatic
        fun of(inputStream: InputStream, directory: File): FileData {
            require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't a directory" }

            val file = createTempFile(directory = directory).apply {
                outputStream().use { inputStream.copyTo(it) }
            }

            return FileData(file)
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
}