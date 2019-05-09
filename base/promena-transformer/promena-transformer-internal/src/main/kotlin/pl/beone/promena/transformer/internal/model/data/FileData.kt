package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataException
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

        isAvailable()

        return try {
            File(location).readBytes()
        } catch (e: Exception) {
            throw DataException("Couldn't read bytes from <$location>", e)
        }
    }

    override fun getLocation(): URI =
            uri

    override fun isAvailable() {
        if (!File(uri).exists()) {
            throw DataAccessibilityException("File <${getLocation()}> doesn't exist")
        }
    }
}