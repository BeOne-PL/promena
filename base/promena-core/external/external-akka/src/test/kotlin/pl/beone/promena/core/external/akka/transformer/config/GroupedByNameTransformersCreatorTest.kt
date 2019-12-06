package pl.beone.promena.core.external.akka.transformer.config

import akka.actor.ActorRef
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class GroupedByNameTransformersCreatorTest {

    @Test
    fun create() {
        val libreOfficeConverterTransformer = mockk<Transformer>()
        val msOfficeConverterTransformer = mockk<Transformer>()
        val zxingBarcodeTransformer = mockk<Transformer>()

        val libreOfficeConverterTransformerId = ("converter" to "libre-office").toTransformerId()
        val msOfficeConverterTransformerId = ("converter" to "ms-office").toTransformerId()
        val zxingBarcodeTransformerId = ("barcode" to "zxing").toTransformerId()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformerId(libreOfficeConverterTransformer) } returns libreOfficeConverterTransformerId
            every { getTransformerId(msOfficeConverterTransformer) } returns msOfficeConverterTransformerId
            every { getTransformerId(zxingBarcodeTransformer) } returns zxingBarcodeTransformerId

            every { getActors(libreOfficeConverterTransformer) } returns 1
            every { getActors(msOfficeConverterTransformer) } returns 2
            every { getActors(zxingBarcodeTransformer) } returns 3

            every { getPriority(libreOfficeConverterTransformer) } returns 3
            every { getPriority(msOfficeConverterTransformer) } returns 2
            every { getPriority(zxingBarcodeTransformer) } returns 1
        }

        val actorRef = mockk<ActorRef>()
        val actorRef2 = mockk<ActorRef>()

        val actorCreator = mockk<ActorCreator> {
            every { create("converter", any(), 2) } returns actorRef
            every { create("barcode", any(), 3) } returns actorRef2
        }

        GroupedByNameTransformersCreator(transformerConfig, mockk(), mockk(), actorCreator)
            .create(listOf(libreOfficeConverterTransformer, msOfficeConverterTransformer, zxingBarcodeTransformer)) shouldBe
                listOf(
                    TransformerActorDescriptor(libreOfficeConverterTransformerId, actorRef, 2),
                    TransformerActorDescriptor(msOfficeConverterTransformerId, actorRef, 2),
                    TransformerActorDescriptor(zxingBarcodeTransformerId, actorRef2, 3)
                )
    }

    @Test
    fun `create _ duplicated id _ should throw IllegalStateException`() {
        val libreOfficeConverterTransformer = mockk<Transformer>()
        val libreOfficeConverter2Transformer = mockk<Transformer>()
        val msOfficeConverterTransformer = mockk<Transformer>()
        val zxingBarcodeTransformer = mockk<Transformer>()
        val dssDocumentSignerTransformer = mockk<Transformer>()
        val dssDocumentSigner2Transformer = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformerId(libreOfficeConverterTransformer) } returns ("converter" to "libre-office").toTransformerId()
            every { getTransformerId(libreOfficeConverter2Transformer) } returns ("converter" to "libre-office").toTransformerId()
            every { getTransformerId(msOfficeConverterTransformer) } returns ("converter" to "ms-office").toTransformerId()
            every { getTransformerId(zxingBarcodeTransformer) } returns ("barcode" to "zxing").toTransformerId()
            every { getTransformerId(dssDocumentSignerTransformer) } returns ("document-signer" to "dss").toTransformerId()
            every { getTransformerId(dssDocumentSigner2Transformer) } returns ("document-signer" to "dss").toTransformerId()
        }

        with(shouldThrow<IllegalStateException> {
            GroupedByNameTransformersCreator(transformerConfig, mockk(), mockk(), mockk())
                .create(
                    listOf(
                        libreOfficeConverterTransformer,
                        libreOfficeConverter2Transformer,
                        msOfficeConverterTransformer,
                        zxingBarcodeTransformer,
                        dssDocumentSignerTransformer,
                        dssDocumentSigner2Transformer
                    )
                )
        }) {
            with(message!!.split("\n")) {
                this[0] shouldContain "Detected <2> transformers with duplicated id:"
                this[1] shouldContain "> (converter, libre-office): "
                this[2] shouldContain "> (document-signer, dss): "
            }
        }
    }

    @Test
    fun `create _ duplicated priority id _ should throw IllegalStateException`() {
        val libreOfficeConverterTransformer = mockk<Transformer>()
        val msOfficeConverterTransformer = mockk<Transformer>()
        val zxingBarcodeTransformer = mockk<Transformer>()
        val imageMagickSignerTransformer = mockk<Transformer>()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformerId(libreOfficeConverterTransformer) } returns ("converter" to "libre-office").toTransformerId()
            every { getActors(libreOfficeConverterTransformer) } returns 1
            every { getPriority(libreOfficeConverterTransformer) } returns 1

            every { getTransformerId(msOfficeConverterTransformer) } returns ("converter" to "ms-office").toTransformerId()
            every { getActors(msOfficeConverterTransformer) } returns 1
            every { getPriority(msOfficeConverterTransformer) } returns 1

            every { getTransformerId(imageMagickSignerTransformer) } returns ("converter" to "image-magick").toTransformerId()
            every { getActors(imageMagickSignerTransformer) } returns 1
            every { getPriority(imageMagickSignerTransformer) } returns 2

            every { getTransformerId(zxingBarcodeTransformer) } returns ("barcode" to "zxing").toTransformerId()
            every { getActors(zxingBarcodeTransformer) } returns 1
            every { getPriority(zxingBarcodeTransformer) } returns 1
        }

        with(
            shouldThrow<IllegalStateException> {
                GroupedByNameTransformersCreator(transformerConfig, mockk(), mockk(), mockk())
                    .create(
                        listOf(
                            libreOfficeConverterTransformer,
                            msOfficeConverterTransformer,
                            zxingBarcodeTransformer,
                            imageMagickSignerTransformer
                        )
                    )
            }
        ) {
            with(message!!.split("\n")) {
                this[0] shouldContain "Detected <1> transformers with duplicated priority:"
                this[1] shouldContain "> (converter) [priority: 1]: "
            }
        }
    }

    @Test
    fun `create _ no transformer _ should throw IllegalStateException`() {
        shouldThrow<IllegalStateException> {
            GroupedByNameTransformersCreator(mockk(), mockk(), mockk(), mockk())
                .create(emptyList())
        }.message shouldContain "No transformer was found. You must add at least <1> transformer"
    }
}