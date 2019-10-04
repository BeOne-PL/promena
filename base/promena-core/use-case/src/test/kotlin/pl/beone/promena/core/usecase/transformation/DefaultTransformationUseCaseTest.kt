package pl.beone.promena.core.usecase.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.core.applicationmodel.exception.communication.external.manager.ExternalCommunicationManagerValidationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_JSON
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.emptyDataDescriptor
import pl.beone.promena.transformer.contract.data.emptyTransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.net.URI

class DefaultTransformationUseCaseTest {

    companion object {
        private val targetMediaType = APPLICATION_PDF
        private val parameters = emptyParameters() + ("key" to "value")

        private val transformation = singleTransformation("test", targetMediaType, parameters)
        private val transformedDataDescriptor = emptyTransformedDataDescriptor()
    }

    @Test
    fun transform() {
        val dataDescriptor = singleDataDescriptor(mockk(), TEXT_PLAIN, emptyMetadata())

        val externalCommunicationId = "memory"
        val externalCommunicationParameters = communicationParameters(externalCommunicationId)
        val incomingCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptor, externalCommunicationParameters) } returns dataDescriptor
        }
        val outgoingCommunicationConverter = mockk<OutgoingExternalCommunicationConverter> {
            every { convert(transformedDataDescriptor, externalCommunicationParameters) } returns transformedDataDescriptor
        }

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } returns
                    ExternalCommunication(externalCommunicationId, incomingCommunicationConverter, outgoingCommunicationConverter)
        }

        val transformerService = mockk<TransformationService> {
            every { transform(transformation, dataDescriptor) } returns transformedDataDescriptor
        }

        DefaultTransformationUseCase(externalCommunicationManager, transformerService)
            .transform(transformation, dataDescriptor, externalCommunicationParameters) shouldBe transformedDataDescriptor
    }

    @Test
    fun `transform _ externalCommunicationManager throws ExternalCommunicationManagerException _ should throw exception with error message without stack to hide Promena implementation details`() {
        val dataDescriptor = emptyDataDescriptor()

        val externalCommunicationId = "memory"
        val externalCommunicationParameters = communicationParameters(externalCommunicationId)

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } throws
                    ExternalCommunicationManagerValidationException("Exception occurred", RuntimeException("Stack exception"))
        }

        shouldThrow<TransformationException> {
            DefaultTransformationUseCase(externalCommunicationManager, mockk())
                .transform(transformation, dataDescriptor, externalCommunicationParameters)
        }.let {
            it.message!!.split("\n").let { messages ->
                messages[0] shouldBe "Couldn't perform transformation <Single(transformerId=TransformerId(name=test, subName=null), targetMediaType=MediaType(mimeType=application/pdf, charset=UTF-8), parameters=MapParameters(parameters={key=value}))> <0 source(s)>: [] <MapCommunicationParameters(parameters={id=memory})> because an error occurred. Check Promena logs for more details"
                messages[1] shouldBe "# Exception occurred"
            }
            it.causeClass shouldBe ExternalCommunicationManagerValidationException::class.java
            it.cause shouldBe null
        }
    }

    @Test
    fun `transform _ transformationService throws TransformationException _ should unwrap and throw exception only with message to hide Promena implementation details`() {
        val data = mockk<Data> {
            every { getLocation() } throws UnsupportedOperationException()
        }
        val data2 = mockk<Data> {
            every { getLocation() } returns URI("file:/tmp/test.tmp")
        }
        val dataDescriptor = singleDataDescriptor(data, TEXT_PLAIN, emptyMetadata()) +
                singleDataDescriptor(data2, APPLICATION_JSON, emptyMetadata() + ("key" to "value"))

        val externalCommunicationId = "memory"
        val externalCommunicationParameters = communicationParameters(externalCommunicationId)
        val incomingCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptor, externalCommunicationParameters) } returns dataDescriptor
        }

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(externalCommunicationId) } returns
                    ExternalCommunication(externalCommunicationId, incomingCommunicationConverter, mockk())
        }

        val transformerService = mockk<TransformationService> {
            every { transform(transformation, dataDescriptor) } throws
                    TransformationException(
                        transformation,
                        "Transformation isn't supported",
                        TransformationNotSupportedException::class.java,
                        TransformationNotSupportedException.custom("Not supported")
                    )
        }

        shouldThrow<TransformationException> {
            DefaultTransformationUseCase(externalCommunicationManager, transformerService)
                .transform(transformation, dataDescriptor, externalCommunicationParameters)
        }.let {
            it.message!!.split("\n").let { messages ->
                messages[0] shouldBe "Couldn't perform transformation <Single(transformerId=TransformerId(name=test, subName=null), targetMediaType=MediaType(mimeType=application/pdf, charset=UTF-8), parameters=MapParameters(parameters={key=value}))> <2 source(s)>: [<no location, MediaType(mimeType=text/plain, charset=UTF-8)>, <file:/tmp/test.tmp, MediaType(mimeType=application/json, charset=UTF-8), MapMetadata(metadata={key=value})>] <MapCommunicationParameters(parameters={id=memory})>"
                messages[1] shouldBe "# Transformation isn't supported"
            }
            it.causeClass shouldBe TransformationNotSupportedException::class.java
            it.cause shouldBe null
        }
    }
}