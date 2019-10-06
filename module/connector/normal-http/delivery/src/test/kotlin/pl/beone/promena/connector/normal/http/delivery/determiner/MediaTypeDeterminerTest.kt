package pl.beone.promena.connector.normal.http.delivery.determiner

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import kotlin.text.Charsets.UTF_16

class MediaTypeDeterminerTest {

    @Test
    fun `determine _ given dataDescriptor-mediaType-mimeType and dataDescriptor-mediaType-charset`() {
        MediaTypeDeterminer.determine(
            "", (createHeaders(APPLICATION_PDF.mimeType, UTF_16.name(), null) + additionalHeaders).toHttpHeaders()
        ) shouldBe mediaType(APPLICATION_PDF.mimeType, UTF_16.name())
    }

    @Test
    fun `determine _ given dataDescriptor-mediaType-mimeType, without dataDescriptor-mediaType-charset _ should use UTF-8 charset`() {
        MediaTypeDeterminer.determine(
            "", (createHeaders(APPLICATION_PDF.mimeType, null, null) + additionalHeaders).toHttpHeaders()
        ) shouldBe APPLICATION_PDF
    }

    @Test
    fun `determine _ Content-Type header with charset`() {
        MediaTypeDeterminer.determine(
            "", (createHeaders(null, null, "text/plain; charset=utf-16") + additionalHeaders).toHttpHeaders()
        ) shouldBe mediaType(TEXT_PLAIN.mimeType, UTF_16)
    }

    @Test
    fun `determine _ Content-Type header without charset _ should use UTF-8 charset`() {
        MediaTypeDeterminer.determine(
            "", (createHeaders(null, null, "text/plain") + additionalHeaders).toHttpHeaders()
        ) shouldBe TEXT_PLAIN
    }

    @Test
    fun `determine _ incorrect mime type name in Content-Type header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                "", (createHeaders(null, null, "text/plain;") + additionalHeaders).toHttpHeaders()
            )
        }.message shouldBe "Part header <Content-Type> has incorrect format <text/plain;>. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)"
    }

    @Test
    fun `determine _ no dataDescriptor-mediaType-mimeType or Content-Type header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                "field", (createHeaders(null, null, null) + additionalHeaders).toHttpHeaders()
            )
        }.message shouldBe "Part <field> headers don't contain <dataDescriptor-mediaType-mimeType> or <Content-Type> header"
    }

    @Test
    fun `determine _ Content-Type with incorrect chars _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            MediaTypeDeterminer.determine(
                "field", (createHeaders(null, null, "text/plain ") + additionalHeaders).toHttpHeaders()
            )
        }.message shouldBe "Part header <Content-Type> has incorrect format <text/plain >. Acceptable formats: (<mime type>; charset=<charset>) or (<mime type>)"
    }

    private fun createHeaders(mimeType: String?, charset: String?, contentType: String?): Map<String, String> =
        mapOf(
            "dataDescriptor-mediaType-mimeType" to mimeType,
            "dataDescriptor-mediaType-charset" to charset,
            CONTENT_TYPE to contentType
        )
            .filterNot { (_, value) -> value == null }
            .map { (key, value) -> key to value!! }
            .toMap()
}