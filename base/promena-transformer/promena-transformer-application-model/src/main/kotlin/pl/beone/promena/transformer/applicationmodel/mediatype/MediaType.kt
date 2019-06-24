package pl.beone.promena.transformer.applicationmodel.mediatype

import java.nio.charset.Charset

data class MediaType(val mimeType: String,
                     val charset: Charset) {

    companion object {

        @JvmStatic
        fun create(mimeType: String, charset: Charset = Charsets.UTF_8): MediaType =
                MediaType(mimeType, charset)

    }

}