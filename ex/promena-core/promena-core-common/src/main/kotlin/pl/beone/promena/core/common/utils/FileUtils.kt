package pl.beone.promena.core.common.utils

import java.io.File
import java.io.IOException
import java.net.URI

fun URI.verifyIfItIsDirectoryAndYouCanCreateFile() {
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
