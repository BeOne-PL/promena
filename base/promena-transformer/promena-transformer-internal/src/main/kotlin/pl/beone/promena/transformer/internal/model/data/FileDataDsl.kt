package pl.beone.promena.transformer.internal.model.data

import java.io.File
import java.io.InputStream
import java.net.URI

fun URI.toFileData(): FileData =
        FileData.of(this)

fun File.toFileData(): FileData =
        FileData.of(this)

fun InputStream.toFileData(directoryUri: URI): FileData =
        FileData.of(this, directoryUri)

fun InputStream.toFileData(directoryFile: File): FileData =
        FileData.of(this, directoryFile)