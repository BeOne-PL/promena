package pl.beone.promena.transformer.internal.model.data

import java.io.File

internal fun String.createTmpFile(directory: File = createTempDir()): File =
    createTempFile(directory = directory).apply {
        writeText(this@createTmpFile)
    }