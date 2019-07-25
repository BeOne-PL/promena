package pl.beone.promena.core.external.akka.actor.transformer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformersCouldNotTransformException
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.util.*
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerCouldNotTransformException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.TransformerId
import java.util.concurrent.TimeoutException

private data class NoSuitedTransformersException(val transformerExceptionsAccumulator: TransformerExceptionsAccumulator) : RuntimeException()

class GroupedByNameTransformerActor(private val transformerName: String,
                                    private val transformerDescriptors: List<TransformerDescriptor>,
                                    private val internalCommunicationConverter: InternalCommunicationConverter) : AbstractLoggingActor() {

    override fun createReceive(): Receive =
        receiveBuilder()
                .match(ToTransformMessage::class.java) {
                    try {
                        sender.tell(
                                TransformedMessage(performTransformation(it.transformerId, it.dataDescriptor, it.targetMediaType, it.parameters)),
                                self
                        )
                    } catch (e: Exception) {
                        sender.tell(
                                Status.Failure(processException(e, it.parameters)),
                                self
                        )
                    } catch (e: Error) {
                        sender.tell(Status.Failure(e), self)
                    }
                }
                .matchAny {}
                .build()

    private fun performTransformation(transformationTransformerId: TransformerId,
                                      dataDescriptor: DataDescriptor,
                                      targetMediaType: MediaType,
                                      parameters: Parameters): TransformedDataDescriptor {
        val (transformedDataDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            determineTransformer(transformationTransformerId, dataDescriptor, targetMediaType, parameters)
                    .let { transformer -> transformer.transform(dataDescriptor, targetMediaType, parameters) }
                    .let { transformedDataDescriptor -> internalCommunicationConverter.convert(dataDescriptor, transformedDataDescriptor) }
        }

        if (log().isDebugEnabled) {
            // I have to use replace functions because Akka debug handles only 4 arguments
            log().debug("Transformed <:1, :2> from <:3 MB, :4 source(s)> to <:5 MB, :6 result(s)> in <:7 s>"
                                .replace(":1", targetMediaType.toString())
                                .replace(":2", parameters.toString())
                                .replace(":3", dataDescriptor.descriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":4", dataDescriptor.descriptors.size.toString())
                                .replace(":5", transformedDataDescriptor.descriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":6", transformedDataDescriptor.descriptors.size.toString())
                                .replace(":7", measuredTimeMs.toSeconds().toString()))
        }

        return transformedDataDescriptor
    }

    private fun determineTransformer(transformationTransformerId: TransformerId,
                                     dataDescriptor: DataDescriptor,
                                     targetMediaType: MediaType,
                                     parameters: Parameters): Transformer =
        try {
            if (transformationTransformerId.subName == null) {
                getGeneralTransformer(dataDescriptor, targetMediaType, parameters)
            } else {
                getDetailedTransformer(transformationTransformerId, dataDescriptor, targetMediaType, parameters)
            }
        } catch (e: NoSuitedTransformersException) {
            throw createException(transformationTransformerId, dataDescriptor, targetMediaType, parameters, e.transformerExceptionsAccumulator)
        }

    private fun getGeneralTransformer(dataDescriptor: DataDescriptor,
                                      targetMediaType: MediaType,
                                      parameters: Parameters): Transformer {
        val transformerExceptionsAccumulator = TransformerExceptionsAccumulator()
        return transformerDescriptors.map { it.transformer }
                       .firstOrNull { transformer ->
                           try {
                               transformer.canTransform(dataDescriptor, targetMediaType, parameters)
                               true
                           } catch (e: TransformerCouldNotTransformException) {
                               transformerExceptionsAccumulator.add(transformer, e.message!!)
                               false
                           }
                       } ?: throw NoSuitedTransformersException(transformerExceptionsAccumulator)
    }

    private fun getDetailedTransformer(transformationTransformerId: TransformerId,
                                       dataDescriptor: DataDescriptor,
                                       targetMediaType: MediaType,
                                       parameters: Parameters): Transformer {
        val transformerExceptionsAccumulator = TransformerExceptionsAccumulator()

        val suitedTransformerDescriptor = try {
            transformerDescriptors.get(transformationTransformerId)
        } catch (e: NoSuchElementException) {
            transformerDescriptors.forEach { transformerExceptionsAccumulator.addUnsuitable(it, transformationTransformerId) }
            throw NoSuitedTransformersException(transformerExceptionsAccumulator)
        }

        return try {
            val suitedTransformer = suitedTransformerDescriptor.transformer
            suitedTransformer.canTransform(dataDescriptor, targetMediaType, parameters)
            suitedTransformer
        } catch (e: TransformerCouldNotTransformException) {
            transformerExceptionsAccumulator.add(suitedTransformerDescriptor.transformer, e.message!!)
            (transformerDescriptors - suitedTransformerDescriptor).forEach { transformerExceptionsAccumulator.addUnsuitable(it, transformationTransformerId) }
            throw NoSuitedTransformersException(transformerExceptionsAccumulator)
        }
    }

    private fun List<TransformerDescriptor>.get(transformerId: TransformerId): TransformerDescriptor =
        first { it.transformerId == transformerId }

    private fun createException(transformationTransformerId: TransformerId,
                                dataDescriptor: DataDescriptor,
                                targetMediaType: MediaType,
                                parameters: Parameters,
                                transformerExceptionsAccumulator: TransformerExceptionsAccumulator): TransformersCouldNotTransformException =
        TransformersCouldNotTransformException(
                "There is no <$transformerName> transformer that can transform data descriptors [${dataDescriptor.generateDescription()}] using <$transformationTransformerId, $targetMediaType, $parameters>: ${transformerExceptionsAccumulator.generateDescription()}"
        )

    private fun DataDescriptor.generateDescription(): String =
        descriptors.joinToString(", ") {
            try {
                "<${it.data.getLocation()}, ${it.mediaType}, ${it.metadata}>"
            } catch (e: UnsupportedOperationException) {
                "<no location, ${it.mediaType}>"
            }
        }

    private fun processException(exception: Exception, parameters: Parameters): Exception =
        when (exception) {
            is TimeoutException -> TransformerTimeoutException("Couldn't transform because <$transformerName> transformer timeout <${parameters.getTimeoutOrInfiniteIfNotFound()}> has been reached")
            else                -> exception
        }
}