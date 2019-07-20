package pl.beone.promena.core.usecase.transformation

import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class DefaultTransformationUseCase(private val externalCommunicationManager: ExternalCommunicationManager,
                                   private val transformationService: TransformationService)
    : TransformationUseCase {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultTransformationUseCase::class.java)
    }

    override fun transform(transformation: Transformation,
                           dataDescriptor: DataDescriptor,
                           externalCommunicationParameters: CommunicationParameters): TransformedDataDescriptor {
        try {
            val (_, incomingExternalCommunicationConverter, outgoingExternalCommunicationConverter) =
                    externalCommunicationManager.getCommunication(externalCommunicationParameters.getId())

            return dataDescriptor
                    .let { incomingExternalCommunicationConverter.convert(it, externalCommunicationParameters) }
                    .let { transformationService.transform(transformation, it) }
                    .let { outgoingExternalCommunicationConverter.convert(it, externalCommunicationParameters) }
        } catch (e: Exception) {
            logger.error("Couldn't transform <{}, {}>", externalCommunicationParameters, transformation, e)

            // unwrap expected exception to not show user unnecessary information
            if (e is TransformationException) {
                throw TransformationException(e.message!!)
            } else {
                throw e
            }
        }
    }
}