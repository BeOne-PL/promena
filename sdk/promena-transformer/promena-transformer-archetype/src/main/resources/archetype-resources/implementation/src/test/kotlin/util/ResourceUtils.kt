package ${package}.util

import java.io.InputStream

internal fun getResourceAsBytes(path: String): ByteArray =
    getResourceAsInputStream(path).readAllBytes()

internal fun getResourceAsInputStream(path: String): InputStream =
    object {}::class.java.getResourceAsStream(path) ?: throw IllegalArgumentException("There is no <$path> resource")