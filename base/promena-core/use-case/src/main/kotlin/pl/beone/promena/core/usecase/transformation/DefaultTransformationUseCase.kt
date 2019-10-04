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

            return incomingExternalCommunicationConverter.convert(dataDescriptor, externalCommunicationParameters)
                .let { convertedDataDescriptor -> transformationService.transform(transformation, convertedDataDescriptor) }
                .let { transformedDataDescriptor ->
                    outgoingExternalCommunicationConverter.convert(transformedDataDescriptor, externalCommunicationParameters)
                }
        } catch (e: Exception) {
            val processedException = processExceptionMessage(e)
            val exceptionMessage = "Couldn't perform transformation " +
                    "${generateTransformationExceptionDescription(transformation, dataDescriptor)} <$externalCommunicationParameters>"

            if (e is TransformationException) {
                throw processExpectedException(transformation, exceptionMessage, processedException, e)
            } else {
                // unwrap expected exception to hide unnecessary information from user
                throw processUnexpectedException(transformation, exceptionMessage, processedException, e)
            }
        }
    }

    private fun processExpectedException(
        transformation: Transformation,
        exceptionMessage: String,
        processedException: String,
        exception: TransformationException
    ): TransformationException {
        logger.error { exceptionMessage + "\n" + processedException }

        return TransformationException(
            transformation,
            exceptionMessage + "\n" + processedException,
            exception.cause?.javaClass
        )
    }

    private fun processUnexpectedException(
        transformation: Transformation,
        exceptionMessage: String,
        processedException: String,
        exception: Exception
    ): Exception {
        logger.error(exception) { exceptionMessage }

        return TransformationException(
            transformation,
            exceptionMessage + " because an error occurred. Check Promena logs for more details" + "\n" + processedException,
            exception.javaClass
        )
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

    private fun processExceptionMessage(exception: Exception): String =
        (exception.message ?: "No exception message available")
            .split("\n")
            .joinToString("\n") { "# $it" }
}