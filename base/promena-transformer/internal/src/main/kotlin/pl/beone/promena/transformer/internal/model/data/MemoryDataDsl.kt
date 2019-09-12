@file:JvmName("MemoryDataDsl")

package pl.beone.promena.transformer.internal.model.data

import java.io.InputStream

fun memoryData(byteArray: ByteArray): MemoryData =
    MemoryData.of(byteArray)

fun memoryData(inputStream: InputStream): MemoryData =
    MemoryData.of(inputStream)

fun ByteArray.toMemoryData(): MemoryData =
    MemoryData.of(this)

fun InputStream.toMemoryData(): MemoryData =
    MemoryData.of(this)

fun String.toMemoryData(): MemoryData =
    MemoryData.of(this.toByteArray())