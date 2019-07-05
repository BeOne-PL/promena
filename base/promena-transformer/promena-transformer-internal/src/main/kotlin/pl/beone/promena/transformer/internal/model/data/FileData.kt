package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import pl.beone.promena.transformer.contract.model.Data
import java.io.File
import java.net.URI

data class FileData(private val uri: URI) : Data {

    init {
        if (uri.scheme == "file") {
            uri
        } else {
            throw UnsupportedOperationException("Location URI <$uri> has <${uri.scheme}> scheme but this implementation supports only <file> scheme")
        }
    }

    override fun getBytes(): ByteArray {
        val location = getLocation()

        isAccessible()

        return try {
            File(location).readBytes()
        } catch (e: Exception) {
            throw DataReadException("Couldn't read bytes from <$location>", e)
        }
    }

    override fun getLocation(): URI =
            uri

    override fun isAccessible() {
        if (!File(uri).exists()) {
            throw DataAccessibilityException("File <${getLocation()}> doesn't exist")
        }
    }

    override fun delete() {
        try {
            if (!File(uri).delete()) {
                throw DataDeleteException("Couldn't delete <$uri> file. Maybe file doesn't exist")
            }
        } catch (e: Exception) {
            throw when (e) {
                is DataDeleteException -> e
                else                   -> DataDeleteException("Couldn't delete <$uri> file", e)
            }
        }
    }
}