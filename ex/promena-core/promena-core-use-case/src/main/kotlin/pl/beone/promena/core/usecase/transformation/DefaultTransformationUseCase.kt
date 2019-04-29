package pl.beone.promena.core.usecase.transformation

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.CommunicationValidator
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.TransformerService

class DefaultTransformationUseCase(private val descriptorSerializationService: DescriptorSerializationService,
                                   private val communicationValidator: CommunicationValidator,
                                   private val incomingCommunicationConverter: IncomingCommunicationConverter,
                                   private val transformerService: TransformerService,
                                   private val outgoingCommunicationConverter: OutgoingCommunicationConverter)
    : TransformationUseCase {

    override fun transform(transformerId: String, bytes: ByteArray, communicationParameters: CommunicationParameters): ByteArray {
        val transformationDescriptor = descriptorSerializationService.deserialize(bytes)

        communicationValidator.validate(transformationDescriptor.dataDescriptors, communicationParameters)

        val convertedDataDescriptors =
                transformationDescriptor.dataDescriptors.map { incomingCommunicationConverter.convert(it, communicationParameters) }

        val transformedDataDescriptors = transformerService.transform(transformerId,
                                                                      convertedDataDescriptors,
                                                                      transformationDescriptor.targetMediaType,
                                                                      transformationDescriptor.parameters)

        val convertedTransformedDataDescriptors =
                transformedDataDescriptors.map { outgoingCommunicationConverter.convert(it, communicationParameters) }

        return descriptorSerializationService.serialize(convertedTransformedDataDescriptors)
    }

}