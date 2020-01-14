@file:JvmName("FileDataDsl")

package pl.beone.promena.transformer.internal.model.data.file

import java.io.File
import java.io.InputStream

fun fileData(file: File): FileData =
    FileData.of(file)

/**
 * @see [FileData.of]
 */
fun fileData(inputStream: InputStream, directory: File): FileData =
    FileData.of(inputStream, directory)

fun File.toFileData(): FileData =
    FileData.of(this)

/**
 * @see [FileData.of]
 */
fun InputStream.toFileData(directory: File): FileData =
    FileData.of(this, directory)

fun FileData.getFile(): File =
    File(getLocation())