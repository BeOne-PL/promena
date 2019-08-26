package pl.beone.promena.transformer.applicationmodel.mediatype

import java.nio.charset.Charset

data class MediaType private constructor(
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