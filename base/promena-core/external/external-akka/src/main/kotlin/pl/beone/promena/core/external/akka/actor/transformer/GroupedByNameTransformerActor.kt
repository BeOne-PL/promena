package pl.beone.promena.core.external.akka.actor.transformer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.extension.getTimeoutOrInfiniteIfNotFound
import pl.beone.promena.core.external.akka.extension.toPrettyString
import pl.beone.promena.core.external.akka.util.measureTimeMillisWithContent
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId
import pl.beone.promena.transformer.internal.extension.format
import pl.beone.promena.transformer.internal.extension.toPrettyString
import pl.beone.promena.transformer.internal.extension.toSeconds
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
                "Transformed in <${measuredTimeMs.toSeconds().format(3)} s>\n" +
                        "> Transformation: (id=${transformationTransformerId.toPrettyString()}, targetMediaType=${targetMediaType.toPrettyString()}, parameters=${parameters.getAll()})\n" +
                        "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toPrettyString()}\n" +
                        "> Transformed data descriptor <${transformedDataDescriptor.descriptors.size}>: ${transformedDataDescriptor.toPrettyString()}"
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
            throw TransformationNotSupportedException.custom(
                "Transformer ${transformer.javaClass.canonicalName}(${transformationTransformerId.name}, ${transformationTransformerId.subName}) doesn't support this transformation: ${e.message}"
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
            ?: throw createGeneralException(transformerExceptionsAccumulator)
    }

    private fun createGeneralException(transformerExceptionsAccumulator: TransformerExceptionsAccumulator): TransformationNotSupportedException =
        TransformationNotSupportedException.custom(
            "There is no transformer in group <$transformerName> that support this transformation\n" +
                    transformerExceptionsAccumulator.generateDescription()
        )

    private fun List<TransformerDescriptor>.get(transformerId: TransformerId): TransformerDescriptor =
        first { it.transformerId == transformerId }

    private fun processException(exception: Exception, parameters: Parameters): Exception =
        if (exception is TimeoutException) {
            TransformerTimeoutException("Transformer <$transformerName> timeout <${parameters.getTimeoutOrInfiniteIfNotFound().toPrettyString()}> has been reached")
        } else {
            exception
        }
}