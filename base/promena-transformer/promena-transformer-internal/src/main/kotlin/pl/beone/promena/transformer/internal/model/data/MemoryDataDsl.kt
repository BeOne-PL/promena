package pl.beone.promena.transformer.internal.model.data

import java.io.InputStream

fun ByteArray.toMemoryData(): MemoryData =
        MemoryData.of(this)

fun InputStream.toMemoryData(): MemoryData =
        MemoryData.of(this)