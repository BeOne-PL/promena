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

    fun determine(name: String, headers: HttpHeaders): MediaType {
        val flattenedHeaders = headers.map { (key, value) -> key to value.first() }

        return when {
            flattenedHeaders.containsHeaderCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE) ->
                determineBasedOnDataDescriptorHeaders(flattenedHeaders)
            flattenedHeaders.containsHeaderCaseInsensitive(CONTENT_TYPE) ->
                determineBasedOnContentTypeHeader(flattenedHeaders)
            else ->
                throw IllegalStateException("Part <$name> headers don't contain <$DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE> or <$CONTENT_TYPE> header")
        }
    }

    private fun List<Pair<String, String>>.containsHeaderCaseInsensitive(header: String): Boolean =
        any { (key) -> key.compareTo(header, true) == 0 }

    private fun determineBasedOnDataDescriptorHeaders(flattenedHttpHeaders: List<Pair<String, String>>): MediaType {
        val mimeType = flattenedHttpHeaders.getValueCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE) ?: error("Impossible. It's validated earlier")
        val charset = flattenedHttpHeaders.getValueCaseInsensitive(DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET)?.let(Charset::forName) ?: UTF_8
        return mediaType(mimeType, charset)
    }

    private fun determineBasedOnContentTypeHeader(flattenedHttpHeaders: List<Pair<String, String>>): MediaType {
        val contentType = flattenedHttpHeaders.getValueCaseInsensitive(CONTENT_TYPE) ?: error("Impossible. It's validated earlier")

        return (contentTypeRegEx.find(contentType)?.groupValues ?: contentTypeOnlyMimeTypeRegEx.find(contentType)?.groupValues)
            ?.let { groupValues -> createMediaType(groupValues[1], groupValues.getOrNull(2)) }
            ?: throw IllegalStateException("Part header <$CONTENT_TYPE> has incorrect format <$contentType>. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)")
    }

    private fun createMediaType(mimeType: String, charsetName: String?): MediaType =
        mediaType(mimeType, charsetName?.let(Charset::forName) ?: UTF_8)

    private fun <T> List<Pair<String, T>>.getValueCaseInsensitive(key: String): T? =
        firstOrNull { (key_) -> key_.compareTo(key, true) == 0 }?.second
}