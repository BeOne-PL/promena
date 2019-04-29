package pl.beone.promena.transformer.applicationmodel.mediatype

import java.nio.charset.Charset

data class MediaType(val mimeType: String,
                     val charset: Charset) {

    companion object {

        fun create(rawData: String, charset: Charset = Charsets.UTF_8): MediaType =
                MediaType(MimeType(rawData).baseType, charset)

        fun create(rawData: String, charset: String = Charsets.UTF_8.name()): MediaType =
                MediaType(MimeType(rawData).baseType, Charset.forName(charset))

    }

}