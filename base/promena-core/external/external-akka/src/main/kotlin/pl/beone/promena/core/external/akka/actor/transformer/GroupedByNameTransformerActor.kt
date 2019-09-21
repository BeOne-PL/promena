package pl.beone.promena.core.external.akka.actor.transformer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.extension.format
import pl.beone.promena.core.external.akka.extension.getTimeoutOrInfiniteIfNotFound
import pl.beone.promena.core.external.akka.extension.toMB
import pl.beone.promena.core.external.akka.extension.toSeconds
import pl.beone.promena.core.external.akka.util.measureTimeMillisWithContent
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId
import java.util.concurrent.TimeoutException

class GroupedByNameTransformerActor(
    private val transformerName: String,
    private val transformerDescriptors: List<TransformerDescriptor>,
    private val internalCommunicationConverter: InternalCommunicationConverter,
    private val internalCommunicationCleaner: InternalCommunicationCleaner
) : AbstractLoggingActor() {

    override fun createReceive(): Receive =
        receiveBuilder()
            .match(ToTransformMessage::class.java) { (transformedId, dataDescriptor, targetMediaType, parameters) ->
                try {
                    sender.tell(TransformedMessage(performTransformation(transformedId, dataDescriptor, targetMediaType, parameters)), self)
                } catch (e: Exception) {
                    sender.tell(Status.Failure(processException(e, parameters)), self)
                } catch (e: Error) {
                    sender.tell(Status.Failure(e), self)
                }
            }
            .matchAny {}
            .build()

    private fun performTransformation(
        transformationTransformerId: TransformerId,
        dataDescriptor: DataDescriptor,
        targetMediaType: MediaType,
        parameters: Parameters
    ): TransformedDataDescriptor {
        val (transformedDataDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            determineTransformer(transformationTransformerId, dataDescriptor, targetMediaType, parameters)
                .transform(dataDescriptor, targetMediaType, parameters)
        }

        if (log().isDebugEnabled) {
            // I have to use replace functions because Akka debug handles only 4 arguments
            log().debug(
                "Transformed <:1, :2> from <:3 MB, :4 source(s)> to <:5 MB, :6 result(s)> in <:7 s>"
                    .replace(":1", targetMediaType.toString())
                    .replace(":2", parameters.toString())
                    .replace(":3", dataDescriptor.descriptors.map { it.data.getBytes() }.toMB().format(2))
                    .replace(":4", dataDescriptor.descriptors.size.toString())
                    .replace(":5", transformedDataDescriptor.descriptors.map { it.data.getBytes() }.toMB().format(2))
                    .replace(":6", transformedDataDescriptor.descriptors.size.toString())
                    .replace(":7", measuredTimeMs.toSeconds().toString())
            )
        }

        return internalCommunicationConverter.convert(transformedDataDescriptor)
            .also { internalCommunicationCleaner.clean(dataDescriptor, it) }
    }

    private fun determineTransformer(
        transformationTransformerId: TransformerId,
        dataDescriptor: DataDescriptor,
        targetMediaType: MediaType,
        parameters: Parameters
    ): Transformer =
        if (transformationTransformerId.isSubNameSet()) {
            getDetailedTransformer(transformationTransformerId, dataDescriptor, targetMediaType, parameters)
        } else {
            getGeneralTransformer(dataDescriptor, targetMediaType, parameters)
        }

    private fun getDetailedTransformer(
        transformationTransformerId: TransformerId,
        dataDescriptor: DataDescriptor,
        targetMediaType: MediaType,
        parameters: Parameters
    ): Transformer {
        val transformer = transformerDescriptors.get(transformationTransformerId).transformer
        return try {
            transformer
                .also { transformer.isSupported(dataDescriptor, targetMediaType, parameters) }
        } catch (e: TransformationNotSupportedException) {
            throw TransformationNotSupportedException(
                "Transformer doesn't support transforming [${dataDescriptor.generateDescription()}] using <$targetMediaType, $parameters>\n" +
                        "> ${transformer.javaClass.canonicalName}(${transformationTransformerId.name}, ${transformationTransformerId.subName}): ${e.message}"
            )
        }
    }

    private fun getGeneralTransformer(
        dataDescriptor: DataDescriptor,
        targetMediaType: MediaType,
        parameters: Parameters
    ): Transformer {
        val transformerExceptionsAccumulator = TransformerExceptionsAccumulator()
        return transformerDescriptors
            .firstOrNull { transformerDescriptor ->
                try {
                    transformerDescriptor.transformer.isSupported(dataDescriptor, targetMediaType, parameters)
                    true
                } catch (e: TransformationNotSupportedException) {
                    transformerExceptionsAccumulator.add(transformerDescriptor, e.message!!)
                    false
                }
            }?.transformer
            ?: throw createGeneralException(dataDescriptor, targetMediaType, parameters, transformerExceptionsAccumulator)
    }

    private fun createGeneralException(
        dataDescriptor: DataDescriptor,
        targetMediaType: MediaType,
        parameters: Parameters,
        transformerExceptionsAccumulator: TransformerExceptionsAccumulator
    ): TransformationNotSupportedException =
        TransformationNotSupportedException(
            "There is no transformer in group <$transformerName> that support transforming [${dataDescriptor.generateDescription()}] using <$targetMediaType, $parameters>\n" +
                    transformerExceptionsAccumulator.generateDescription()
        )

    private fun List<TransformerDescriptor>.get(transformerId: TransformerId): TransformerDescriptor =
        first { it.transformerId == transformerId }

    private fun DataDescriptor.generateDescription(): String =
        descriptors.joinToString(", ") {
            try {
                "<${it.data.getLocation()}, ${it.mediaType}, ${it.metadata}>"
            } catch (e: UnsupportedOperationException) {
                "<no location, ${it.mediaType}, ${it.metadata}>"
            }
        }

    private fun processException(exception: Exception, parameters: Parameters): Exception =
        if (exception is TimeoutException) {
            TransformerTimeoutException("Couldn't transform because <$transformerName> transformer timeout <${parameters.getTimeoutOrInfiniteIfNotFound()}> has been reached")
        } else {
            exception
        }
}