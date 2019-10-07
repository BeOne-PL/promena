package pl.beone.promena.connector.normal.http.delivery.determiner

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

internal object MediaTypeDeterminer {

    private val contentTypeRegEx = """^(\w+/[-+.\w]+);\s*charset=(.*)$""".toRegex()
    private val contentTypeOnlyMimeTypeRegEx = """^(\w+/[-+.\w]+)$""".toRegex()

    fun determine(fieldName: String?, headers: HttpHeaders): MediaType {
        val singleValueHeaders = headers.toSingleValueMap()

        return when {
            singleValueHeaders.containsHeaderCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE) ->
                determineBasedOnDataDescriptorHeaders(singleValueHeaders)
            singleValueHeaders.containsHeaderCaseInsensitive(CONTENT_TYPE) ->
                determineBasedOnContentTypeHeader(singleValueHeaders)
            else ->
                throw createIllegalStateException(fieldName)
        }
    }

    private fun Map<String, String>.containsHeaderCaseInsensitive(header: String): Boolean =
        any { (key) -> key.compareTo(header, true) == 0 }

    private fun determineBasedOnDataDescriptorHeaders(singleValueHeaders: Map<String, String>): MediaType {
        val mimeType = singleValueHeaders.getValueCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE) ?: error("Impossible. It's validated earlier")
        val charset = singleValueHeaders.getValueCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET)?.let(Charset::forName) ?: UTF_8
        return mediaType(mimeType, charset)
    }

    private fun determineBasedOnContentTypeHeader(singleValueHeaders: Map<String, String>): MediaType {
        val contentType = singleValueHeaders.getValueCaseInsensitive(CONTENT_TYPE) ?: error("Impossible. It's validated earlier")

        return (contentTypeRegEx.find(contentType)?.groupValues ?: contentTypeOnlyMimeTypeRegEx.find(contentType)?.groupValues)
            ?.let { groupValues -> createMediaType(groupValues[1], groupValues.getOrNull(2)) }
            ?: throw IllegalStateException("Part header <$CONTENT_TYPE> has incorrect format <$contentType>. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)")
    }

    private fun createMediaType(mimeType: String, charsetName: String?): MediaType =
        mediaType(mimeType, charsetName?.let(Charset::forName) ?: UTF_8)

    private fun <T> Map<String, T>.getValueCaseInsensitive(key: String): T? =
        entries.firstOrNull { (key_) -> key_.compareTo(key, true) == 0 }?.value

    private fun createIllegalStateException(fieldName: String?): IllegalStateException =
        throw IllegalStateException(
            if (fieldName != null) {
                "Part <$fieldName> headers don't contain <$DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE> or <$CONTENT_TYPE> header"
            } else {
                "Headers of one of the parts don't contain <$DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE> or <$CONTENT_TYPE> header"
            }
        )
}