package pl.beone.promena.transformer.internal.model.data.file

import pl.beone.promena.transformer.contract.model.data.WritableData
import java.io.File
import java.io.OutputStream

class FileWritableData internal constructor(
    file: File
) : WritableData, FileData(file) {

    companion object {
        @JvmStatic
        fun ofEmptyFile(file: File): FileWritableData {
            require(file.exists() && file.isFile) { "File <$file> doesn't exist or isn't a file" }
            require(file.length() == 0L) { "File <$file> isn't empty" }

            return FileWritableData(file)
        }

        @JvmStatic
        fun ofDirectory(directory: File): FileWritableData {
            require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't a directory" }

            return FileWritableData(createTempFile(directory = directory))
        }
    }

    override fun getOutputStream(): OutputStream {
        isAccessible()

        return file.outputStream()
    }
}