package pl.beone.promena.core.usecase.transformation

import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class DefaultTransformationUseCase(private val externalCommunicationManager: ExternalCommunicationManager,
                                   private val transformerService: TransformerService)
    : TransformationUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultTransformationUseCase::class.java)
    }

    override fun transform(transformerId: String,
                           transformationDescriptor: TransformationDescriptor,
                           externalCommunicationParameters: CommunicationParameters): List<TransformedDataDescriptor> {
        try {
            val (_, incomingExternalCommunicationConverter, outgoingExternalCommunicationConverter) =
                    externalCommunicationManager.getCommunication(externalCommunicationParameters.getId())

            val dataDescriptors = transformationDescriptor.dataDescriptors

            val convertedDataDescriptors =
                    incomingExternalCommunicationConverter.convert(dataDescriptors, externalCommunicationParameters)

            val transformedDataDescriptors = transformerService.transform(transformerId,
                                                                          convertedDataDescriptors,
                                                                          transformationDescriptor.targetMediaType,
                                                                          transformationDescriptor.parameters)

            return outgoingExternalCommunicationConverter.convert(transformedDataDescriptors, externalCommunicationParameters)
        } catch (e: Exception) {
            logger.error("Couldn't transform <{}, {}>", transformerId, externalCommunicationParameters, e) // TODO maybe better message

            throw e
        }
    }
}