package pl.beone.promena.core.usecase.transformation

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class DefaultTransformationUseCaseTest {

    companion object {
        private val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        private val dataDescriptor = DataDescriptor(mockk(), MediaTypeConstants.TEXT_PLAIN)
    }

    @Test
    fun transform() {
        val communicationParameters = mockk<CommunicationParameters>()
        val parameters = mockk<Parameters>()
        val transformationDescriptor = TransformationDescriptor(listOf(dataDescriptor), targetMediaType, parameters)
        val transformedDataDescriptor = TransformedDataDescriptor(mockk(), mockk())
        val transformedDataDescriptors = listOf(transformedDataDescriptor)

        val incomingCommunicationConverter = mockk<IncomingCommunicationConverter> {
            every { convert(dataDescriptor, communicationParameters) } returns dataDescriptor
        }
        val transformerService = mockk<TransformerService> {
            every { transform("default", listOf(dataDescriptor), targetMediaType, parameters) } returns transformedDataDescriptors
        }
        val outgoingCommunicationConverter =
                mockk<OutgoingCommunicationConverter> {
                    every { convert(transformedDataDescriptor, communicationParameters) } returns transformedDataDescriptor
                }

        DefaultTransformationUseCase(mockk(), incomingCommunicationConverter, transformerService, outgoingCommunicationConverter)
                .transform("default", transformationDescriptor, communicationParameters) shouldBe transformedDataDescriptors
    }
}