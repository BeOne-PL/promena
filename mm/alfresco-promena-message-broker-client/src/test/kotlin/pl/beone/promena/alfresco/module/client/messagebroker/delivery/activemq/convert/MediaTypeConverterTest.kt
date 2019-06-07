package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import io.kotlintest.shouldBe
import org.junit.Test

import org.junit.Assert.*
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants

class MediaTypeConverterTest {

    companion object {
        private val mediaTypeConverter = MediaTypeConverter()
    }

    @Test
    fun convert() {
        val mediaType = mediaTypeConverter.convert("text/plain", Charsets.ISO_8859_1.toString())

        mediaType.mimeType shouldBe "text/plain"
        mediaType.charset shouldBe Charsets.ISO_8859_1
    }
}