package pl.beone.promena.communication.file.configuration.internal

import org.springframework.core.env.Environment
import java.io.File
import java.io.IOException
import java.net.URI

internal fun Environment.getLocationAndVerify(): URI =
        URI(this.getRequiredProperty("communication.file.location")).apply {
            verifyIfItIsDirectoryAndYouCanCreateFile()
        }

private fun URI.verifyIfItIsDirectoryAndYouCanCreateFile() {
    val scheme = this.scheme
    if (scheme != "file") {
        throw Exception("URI <$this> hasn't <file> scheme")
    }

    try {
        createTempFile(directory = File(this)).delete()
    } catch (e: Exception) {
        throw IOException("Couldn't create file in <$this> location", e)
    }
}