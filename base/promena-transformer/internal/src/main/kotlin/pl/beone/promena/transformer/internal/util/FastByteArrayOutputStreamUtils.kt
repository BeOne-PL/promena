package pl.beone.promena.transformer.internal.util

import com.cedarsoftware.util.FastByteArrayOutputStream

fun createFastByteArrayOutputStream(bytes: ByteArray): FastByteArrayOutputStream =
    FastByteArrayOutputStream(bytes.size)
        .also { it.write(bytes) }