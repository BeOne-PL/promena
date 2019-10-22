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
import pl.beone.promena.transformer.internal.extension.toPrettyString
import pl.beone.promena.transformer.internal.extension.toSimplePrettyString

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

            return incomingExternalCommunicationConverter.convert(dataDescriptor, externalCommunicationParameters)
                .let { convertedDataDescriptor -> transformationService.transform(transformation, convertedDataDescriptor) }
                .let { transformedDataDescriptor ->
                    outgoingExternalCommunicationConverter.convert(transformedDataDescriptor, externalCommunicationParameters)
                }
        } catch (e: Exception) {
            val exceptionMessage = "Couldn't transform\n" +
                    "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                    "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toSimplePrettyString()}\n" +
                    "> External communication: ${externalCommunicationParameters.toPrettyString()}"

            if (e is TransformationException) {
                logger.error { exceptionMessage + "\n" + processExceptionMessage(e) }
                throw e
            } else {
                logger.error(e) { exceptionMessage }
                // unwrap expected exception to hide unnecessary information from user
                throw TransformationException(
                    "Couldn't transform because an error occurred. Check Promena logs for more details" + "\n" + processExceptionMessage(e),
                    e.javaClass
                )
            }
        }
    }

    private fun processExceptionMessage(exception: Exception): String =
        (exception.message ?: "No exception message available")
            .split("\n")
            .joinToString("\n") { "# $it" }
}