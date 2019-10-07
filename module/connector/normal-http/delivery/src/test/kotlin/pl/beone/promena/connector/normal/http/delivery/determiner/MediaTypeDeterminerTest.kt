package pl.beone.promena.connector.normal.http.delivery.determiner

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.connector.normal.http.delivery.extension.toHttpString
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import kotlin.text.Charsets.UTF_16

class MediaTypeDeterminerTest {

    @Test
    fun `determine _ given dataDescriptor-mediaType-mimeType and dataDescriptor-mediaType-charset`() {
        MediaTypeDeterminer.determine(
            null, createHeaders(APPLICATION_PDF.mimeType, UTF_16.name(), null) + additionalHeaders
        ) shouldBe mediaType(APPLICATION_PDF.mimeType, UTF_16.name())
    }

    @Test
    fun `determine _ given dataDescriptor-mediaType-mimeType, without dataDescriptor-mediaType-charset _ should use UTF-8 charset`() {
        MediaTypeDeterminer.determine(
            null, createHeaders(APPLICATION_PDF.mimeType, null, null) + additionalHeaders
        ) shouldBe APPLICATION_PDF
    }

    @Test
    fun `determine _ Content-Type header with charset`() {
        val mediaType = mediaType(TEXT_PLAIN.mimeType, UTF_16)
        MediaTypeDeterminer.determine(
            null, createHeaders(null, null, mediaType.toHttpString()) + additionalHeaders
        ) shouldBe mediaType
    }

    @Test
    fun `determine _ Content-Type header without charset _ should use UTF-8 charset`() {
        val mediaType = TEXT_PLAIN
        MediaTypeDeterminer.determine(
            null, createHeaders(null, null, mediaType.mimeType) + additionalHeaders
        ) shouldBe mediaType
    }

    @Test
    fun `determine _ incorrect mime type name in Content-Type header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                null, createHeaders(null, null, "text/plain;") + additionalHeaders
            )
        }.message shouldBe "Part header <$CONTENT_TYPE> has incorrect format <text/plain;>. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)"
    }

    @Test
    fun `determine _ no dataDescriptor-mediaType-mimeType or Content-Type header with field name _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                "field", createHeaders(null, null, null) + additionalHeaders
            )
        }.message shouldBe "Part <field> headers don't contain <$DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE> or <$CONTENT_TYPE> header"
    }

    @Test
    fun `determine _ no dataDescriptor-mediaType-mimeType or Content-Type header without field name _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                null, createHeaders(null, null, null) + additionalHeaders
            )
        }.message shouldBe "Headers of one of the parts don't contain <$DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE> or <$CONTENT_TYPE> header"
    }

    @Test
    fun `determine _ Content-Type with incorrect chars _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                null, createHeaders(null, null, "text/plain ") + additionalHeaders
            )
        }.message shouldBe "Part header <$CONTENT_TYPE> has incorrect format <text/plain >. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)"
    }

    private fun createHeaders(mimeType: String?, charset: String?, contentType: String?): HttpHeaders =
        mapOf(
            DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE to mimeType,
            DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET to charset,
            CONTENT_TYPE to contentType
        ).filterNotNullValues().toHttpHeaders()
}