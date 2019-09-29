@file:JvmName("FileDataDsl")

package pl.beone.promena.transformer.internal.model.data

import java.io.File
import java.io.InputStream

fun fileData(file: File): FileData =
    FileData.of(file)

fun fileData(inputStream: InputStream, directory: File): FileData =
    FileData.of(inputStream, directory)

fun File.toFileData(): FileData =
    FileData.of(this)

fun InputStream.toFileData(directory: File): FileData =
    FileData.of(this, directory)

fun FileData.getFile(): File =
    File(getLocation())