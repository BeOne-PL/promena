package pl.beone.promena.transformer.applicationmodel.mediatype

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import kotlin.text.Charsets.ISO_8859_1

class MediaTypeDslTest {

    @Test
    fun mediaType() {
        mediaType(APPLICATION_PDF.mimeType, ISO_8859_1) shouldBe
                MediaType.of(APPLICATION_PDF.mimeType, ISO_8859_1)
    }

    @Test
    fun `mediaType _ default charset`() {
        mediaType(APPLICATION_PDF.mimeType) shouldBe
                APPLICATION_PDF
    }

    @Test
    fun `mediaType _ string charset`() {
        mediaType(APPLICATION_PDF.mimeType, ISO_8859_1.name()) shouldBe
                MediaType.of(APPLICATION_PDF.mimeType, ISO_8859_1)
    }

    @Test
    fun `mediaTypeWithCharset _ charset`() {
        APPLICATION_PDF.withCharset(ISO_8859_1) shouldBe
                MediaType.of(APPLICATION_PDF.mimeType, ISO_8859_1)
    }

    @Test
    fun `mediaTypeWithCharset _ charset string`() {
        APPLICATION_PDF.withCharset(ISO_8859_1.name()) shouldBe
                MediaType.of(APPLICATION_PDF.mimeType, ISO_8859_1)
    }
}