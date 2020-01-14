package pl.beone.promena.transformer.internal.model.data.file

import pl.beone.promena.transformer.contract.model.data.WritableData
import java.io.File
import java.io.OutputStream

/**
 * Extends [FileData] with the ability to operate on [OutputStream].
 *
 * @see FileWritableDataDsl
 */
class FileWritableData internal constructor(
    file: File
) : WritableData, FileData(file) {

    companion object {
        /**
         * @throws IllegalArgumentException if [file] doesn't exist or isn't a file
         *                                  or if [file] isn't empty
         */
        @JvmStatic
        fun ofEmptyFile(file: File): FileWritableData {
            require(file.exists() && file.isFile) { "File <$file> doesn't exist or isn't file" }
            require(file.length() == 0L) { "File <$file> isn't empty" }

            return FileWritableData(file)
        }

        /**
         * Creates an empty temporary file in [directory].
         *
         * @throws IllegalArgumentException if [directory] doesn't exist or isn't a directory
         */
        @JvmStatic
        fun ofDirectory(directory: File): FileWritableData {
            require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't directory" }

            return FileWritableData(createTempFile(directory = directory))
        }
    }

    override fun getOutputStream(): OutputStream {
        isAccessible()

        return file.outputStream()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileWritableData) return false
        if (!super.equals(other)) return false
        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}