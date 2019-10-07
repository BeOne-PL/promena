package pl.beone.promena.connector.normal.http.delivery.determiner

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.createTransformationMediaTypeCharset
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.createTransformationMediaTypeMimeType
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.createTransformationTransformerIdNameHeader
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.createTransformationTransformerIdSubNameHeader
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.withCharset
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.contract.transformer.transformerId
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_16

class TransformationDeterminerTest {

    @Test
    fun determine() {
        val transformerId = transformerId("digital signature", "digiSeal")
        val mediaType = APPLICATION_PDF.withCharset(ISO_8859_1)

        val transformerId2 = transformerId("digital signature")
        val mediaType2 = APPLICATION_PDF

        val transformerId3 = transformerId("converter")
        val mediaType3 = TEXT_PLAIN.withCharset(UTF_16)

        TransformationDeterminer.determine(
            createHeaders(1, transformerId.name, transformerId.subName, mediaType.mimeType, mediaType.charset.name()) +
                    createHeaders(2, transformerId2.name, null, mediaType2.mimeType, null) +
                    createHeaders(3, transformerId3.name, null, mediaType3.mimeType, mediaType3.charset.name()) +
                    additionalHeaders
        ).let {
            val transformers = it.transformers
            transformers shouldHaveSize 3
            transformers[0] shouldBe singleTransformation(transformerId, mediaType, emptyParameters())
            transformers[1] shouldBe singleTransformation(transformerId2, mediaType2, emptyParameters())
            transformers[2] shouldBe singleTransformation(transformerId3, mediaType3, emptyParameters())
        }
    }

    @Test
    fun `determine _ single transformation`() {
        val transformerId = transformerId("digital signature", "digiSeal")
        val mediaType = APPLICATION_PDF

        TransformationDeterminer.determine(
            createHeaders(1, transformerId.name, transformerId.subName, mediaType.mimeType, null) + additionalHeaders
        ).let {
            val transformers = it.transformers
            transformers shouldHaveSize 1
            transformers[0] shouldBe singleTransformation(transformerId, mediaType, emptyParameters())
        }
    }

    @Test
    fun `determine _ no transformation group headers _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(additionalHeaders)
        }.message shouldBe "There are no <transformation> group headers"
    }

    @Test
    fun `determine _ no transformerId-name header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(
                createHeaders(1, "digital signature", "digiSeal", APPLICATION_PDF.mimeType, ISO_8859_1.name()) +
                        createHeaders(2, null, null, APPLICATION_PDF.mimeType, null) +
                        additionalHeaders
            )
        }.message shouldBe "There is no header <${createTransformationTransformerIdNameHeader(2)}>"
    }

    @Test
    fun `determine _ no mediaType-mimeType header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(
                createHeaders(1, "digital signature", "digiSeal", null, ISO_8859_1.name()) + additionalHeaders
            )
        }.message shouldBe "There is no header <${createTransformationMediaTypeMimeType(1)}>"
    }

    private fun createHeaders(ordinalNumber: Int, name: String?, subName: String?, mimeType: String?, charset: String?): HttpHeaders =
        mapOf(
            createTransformationTransformerIdNameHeader(ordinalNumber) to name,
            createTransformationTransformerIdSubNameHeader(ordinalNumber) to subName,
            createTransformationMediaTypeMimeType(ordinalNumber) to mimeType,
            createTransformationMediaTypeCharset(ordinalNumber) to charset
        ).filterNotNullValues().toHttpHeaders()
}
