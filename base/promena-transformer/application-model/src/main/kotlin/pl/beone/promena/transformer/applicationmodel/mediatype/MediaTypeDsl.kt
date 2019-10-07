@file:JvmName("MediaTypeDsl")

package pl.beone.promena.transformer.applicationmodel.mediatype

import java.nio.charset.Charset

fun mediaType(mimeType: String, charset: Charset = Charsets.UTF_8): MediaType =
    MediaType.of(mimeType, charset)

fun mediaType(mimeType: String, charset: String): MediaType =
    MediaType.of(mimeType, Charset.forName(charset))

fun MediaType.withCharset(charset: Charset): MediaType =
    MediaType.of(mimeType, charset)

fun MediaType.withCharset(charset: String): MediaType =
    MediaType.of(mimeType, charset)