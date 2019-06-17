package pl.beone.promena.core.usecase.transformation

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class DefaultTransformationUseCaseTest {

    @Test
    fun transform() {
        val communicationParameters = mock<CommunicationParameters>()
        val dataDescriptor = DataDescriptor(mock(), MediaTypeConstants.TEXT_PLAIN)
        val targetMediaType = MediaTypeConstants.APPLICATION_PDF
        val parameters = mock<Parameters>()
        val transformationDescriptor = TransformationDescriptor(listOf(dataDescriptor), targetMediaType, parameters)
        val transformedDataDescriptor = TransformedDataDescriptor(mock(), mock())
        val transformedDataDescriptors = listOf(transformedDataDescriptor)

        val incomingCommunicationConverter =
                mock<IncomingCommunicationConverter> {
                    on { convert(dataDescriptor, communicationParameters) } doReturn dataDescriptor
                }
        val transformerService = mock<TransformerService> {
            on { transform("default", listOf(dataDescriptor), targetMediaType, parameters) } doReturn transformedDataDescriptors
        }
        val outgoingCommunicationConverter =
                mock<OutgoingCommunicationConverter> {
                    on { convert(transformedDataDescriptor, communicationParameters) } doReturn transformedDataDescriptor
                }

        val bytes = DefaultTransformationUseCase(
                mock(),
                incomingCommunicationConverter,
                transformerService,
                outgoingCommunicationConverter)
                .transform("default", transformationDescriptor, communicationParameters)

        assertThat(bytes).isEqualTo(transformedDataDescriptors)
    }
}