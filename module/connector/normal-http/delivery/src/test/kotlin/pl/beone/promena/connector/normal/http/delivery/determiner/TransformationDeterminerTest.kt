package pl.beone.promena.connector.normal.http.delivery.determiner

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_16

class TransformationDeterminerTest {

    @Test
    fun determine() {
        TransformationDeterminer.determine(
            (createHeaders(1, "digital signature", "digiSeal", APPLICATION_PDF.mimeType, ISO_8859_1.name()) +
                    createHeaders(2, "digital signature", null, APPLICATION_PDF.mimeType, null) +
                    createHeaders(3, "converter", null, TEXT_PLAIN.mimeType, UTF_16.name()) +
                    additionalHeaders).toHttpHeaders()
        ).let {
            val transformers = it.transformers
            transformers shouldHaveSize 3
            transformers[0] shouldBe singleTransformation(
                "digital signature",
                "digiSeal",
                mediaType(APPLICATION_PDF.mimeType, ISO_8859_1),
                emptyParameters()
            )
            transformers[1] shouldBe singleTransformation("digital signature", APPLICATION_PDF, emptyParameters())
            transformers[2] shouldBe singleTransformation("converter", mediaType(TEXT_PLAIN.mimeType, UTF_16.name()), emptyParameters())
        }
    }

    @Test
    fun `determine _ single transformation`() {
        TransformationDeterminer.determine(
            (createHeaders(1, "digital signature", "digiSeal", APPLICATION_PDF.mimeType, null) + additionalHeaders).toHttpHeaders()
        ).let {
            val transformers = it.transformers
            transformers shouldHaveSize 1
            transformers[0] shouldBe singleTransformation("digital signature", "digiSeal", APPLICATION_PDF, emptyParameters())
        }
    }

    @Test
    fun `determine _ no transformation group headers _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(additionalHeaders.toHttpHeaders())
        }.message shouldBe "There are no <transformation> group headers"
    }

    @Test
    fun `determine _ no transformerId-name header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(
                (createHeaders(1, "digital signature", "digiSeal", APPLICATION_PDF.mimeType, ISO_8859_1.name()) +
                        createHeaders(2, null, null, APPLICATION_PDF.mimeType, null) +
                        additionalHeaders).toHttpHeaders()
            )
        }.message shouldBe "There is no header <transformation2-transformerId-name>"
    }

    @Test
    fun `determine _ no mediaType-mimeType header _ shouldThrowIllegalStateException`() {
        shouldThrow<IllegalStateException> {
            TransformationDeterminer.determine(
                (createHeaders(1, "digital signature", "digiSeal", null, ISO_8859_1.name()) + additionalHeaders).toHttpHeaders()
            )
        }.message shouldBe "There is no header <transformation-mediaType-mimeType>"
    }

    private fun createHeaders(id: Int, name: String?, subName: String?, mimeType: String?, charset: String?): Map<String, String> {
        val determinedId = if (id != 1) {
            id.toString()
        } else {
            ""
        }

        return mapOf(
            "transformation$determinedId-transformerId-name" to name,
            "transformation$determinedId-transformerId-subName" to subName,
            "transformation$determinedId-mediaType-mimeType" to mimeType,
            "transformation$determinedId-mediaType-charset" to charset
        )
            .filterNot { (_, value) -> value == null }
            .map { (key, value) -> key to value!! }
            .toMap()
    }
}
