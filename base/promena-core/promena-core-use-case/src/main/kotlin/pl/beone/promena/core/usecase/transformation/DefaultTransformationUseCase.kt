package pl.beone.promena.core.usecase.transformation

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.communication.CommunicationValidator
import pl.beone.promena.core.contract.communication.IncomingCommunicationConverter
import pl.beone.promena.core.contract.communication.OutgoingCommunicationConverter
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class DefaultTransformationUseCase(private val communicationValidator: CommunicationValidator,
                                   private val incomingCommunicationConverter: IncomingCommunicationConverter,
                                   private val transformerService: TransformerService,
                                   private val outgoingCommunicationConverter: OutgoingCommunicationConverter)
    : TransformationUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultTransformationUseCase::class.java)
    }

    override fun transform(transformerId: String,
                           transformationDescriptor: TransformationDescriptor,
                           communicationParameters: CommunicationParameters): List<TransformedDataDescriptor> {
        try {
            communicationValidator.validate(transformationDescriptor.dataDescriptors, communicationParameters)

            val convertedDataDescriptors = transformationDescriptor.dataDescriptors.map {
                incomingCommunicationConverter.convert(it, communicationParameters)
            }

            val transformedDataDescriptors = transformerService.transform(transformerId,
                                                                          convertedDataDescriptors,
                                                                          transformationDescriptor.targetMediaType,
                                                                          transformationDescriptor.parameters)

            return transformedDataDescriptors.map { outgoingCommunicationConverter.convert(it, communicationParameters) }
        } catch (e: Exception) {
            logger.error("Couldn't transform <{}, {}>", transformationDescriptor, communicationParameters, e)

            throw e
        }
    }

}