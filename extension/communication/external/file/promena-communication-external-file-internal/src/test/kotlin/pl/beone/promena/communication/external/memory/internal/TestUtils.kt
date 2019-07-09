package pl.beone.promena.communication.external.memory.internal

import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File
import java.net.URI

internal fun String.toFileData(location: URI): FileData =
        FileData(createTempFile(directory = File(location)).apply {
            writeText(this@toFileData)
        }.toURI())