@file:JvmName("FileDataDsl")

package pl.beone.promena.transformer.internal.model.data

import java.io.File
import java.io.InputStream
import java.net.URI

fun fileData(uri: URI): FileData =
    FileData.of(uri)

fun fileData(file: File): FileData =
    FileData.of(file)

fun fileData(inputStream: InputStream, directoryUri: URI): FileData =
    FileData.of(inputStream, directoryUri)

fun fileData(inputStream: InputStream, directoryFile: File): FileData =
    FileData.of(inputStream, directoryFile)

fun URI.toFileData(): FileData =
    FileData.of(this)

fun File.toFileData(): FileData =
    FileData.of(this)

fun InputStream.toFileData(directoryUri: URI): FileData =
    FileData.of(this, directoryUri)

fun InputStream.toFileData(directoryFile: File): FileData =
    FileData.of(this, directoryFile)