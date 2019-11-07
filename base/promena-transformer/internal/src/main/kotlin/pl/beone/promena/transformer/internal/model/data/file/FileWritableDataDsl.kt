@file:JvmName("FileWritableDataDsl")

package pl.beone.promena.transformer.internal.model.data.file

import java.io.File

fun fileWritableDataFromEmptyFile(file: File): FileWritableData =
    FileWritableData.ofEmptyFile(file)

fun fileWritableDataFromDirectory(directory: File): FileWritableData =
    FileWritableData.ofDirectory(directory)

fun File.toFileWritableDataFromEmptyFile(): FileWritableData =
    FileWritableData.ofEmptyFile(this)

fun File.toFileWritableDataFromDirectory(): FileWritableData =
    FileWritableData.ofDirectory(this)