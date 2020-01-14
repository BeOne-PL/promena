package pl.beone.promena.transformer.applicationmodel.mediatype

import java.nio.charset.Charset

/**
 * Represents a two-part identifier for file formats.
 *
 * @property mimeType the name of a type, for example `application/pdf`
 *
 * @see MediaTypeConstants
 * @see MediaTypeDsl
 */
data class MediaType internal constructor(
    val mimeType: String,
    val charset: Charset
) {

    companion object {
        @JvmStatic
        fun of(mimeType: String, charset: Charset): MediaType =
            MediaType(mimeType, charset)

        @JvmStatic
        fun of(mimeType: String, charset: String): MediaType =
            MediaType(mimeType, Charset.forName(charset))

        @JvmStatic
        fun of(mimeType: String): MediaType =
            MediaType(mimeType, Charsets.UTF_8)
    }
}