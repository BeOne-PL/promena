package pl.beone.promena.core.usecase.transformation

import mu.KotlinLogging
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class DefaultTransformationUseCase(
    private val externalCommunicationManager: ExternalCommunicationManager,
    private val transformationService: TransformationService
) : TransformationUseCase {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun transform(
        transformation: Transformation,
        dataDescriptor: DataDescriptor,
        externalCommunicationParameters: CommunicationParameters
    ): TransformedDataDescriptor {
        try {
            val (_, incomingExternalCommunicationConverter, outgoingExternalCommunicationConverter) =
                externalCommunicationManager.getCommunication(externalCommunicationParameters.getId())

            return dataDescriptor
                .let { incomingExternalCommunicationConverter.convert(it, externalCommunicationParameters) }
                .let { transformationService.transform(transformation, it) }
                .let { transformedDataDescriptor ->
                    outgoingExternalCommunicationConverter.convert(transformedDataDescriptor, externalCommunicationParameters)
                }
        } catch (e: Exception) {
            logger.error(e) {
                "Couldn't perform the transformation ${generateTransformationExceptionDescription(transformation, dataDescriptor)} " +
                        "<$externalCommunicationParameters>"
            }

            // unwrap expected exception to not show user unnecessary information
            if (e is TransformationException) {
                throw TransformationException(transformation, e.message!!)
            } else {
                throw TransformationException(
                    transformation,
                    "Couldn't perform the transformation because an error occurred. Check Promena logs for more details: " + e.message
                )
            }
        }
    }

    private fun generateTransformationExceptionDescription(transformation: Transformation, dataDescriptor: DataDescriptor): String =
        "<:1> <:2 source(s)>: [:3]"
            .replace(":1", transformation.toString())
            .replace(":2", dataDescriptor.descriptors.size.toString())
            .replace(":3", dataDescriptor.generateDescription())

    private fun DataDescriptor.generateDescription(): String =
        descriptors.joinToString(", ") {
            try {
                "<${it.data.getLocation()}, ${it.mediaType}, ${it.metadata}>"
            } catch (e: UnsupportedOperationException) {
                "<no location, ${it.mediaType}>"
            }
        }
}