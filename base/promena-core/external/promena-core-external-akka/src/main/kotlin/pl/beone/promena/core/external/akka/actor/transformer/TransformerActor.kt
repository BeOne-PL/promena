package pl.beone.promena.core.external.akka.actor.transformer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerCanNotTransformException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.util.*
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

class TransformerActor(private val transformerId: String,
                       private val transformers: List<Transformer>,
                       private val internalCommunicationConverter: InternalCommunicationConverter) : AbstractLoggingActor() {

    override fun createReceive(): Receive =
            receiveBuilder()
                    .match(ToTransformMessage::class.java) {
                        try {
                            val transformedDataDescriptor = performTransformation(it.dataDescriptor, it.targetMediaType, it.parameters)
                            sender.tell(TransformedMessage(transformedDataDescriptor), self)
                        }
                        catch (e: Exception) {
                            val processedException = processException(e, it.parameters)
                            sender.tell(Status.Failure(processedException), self)
                        } catch (e: Error) {
                            sender.tell(Status.Failure(e), self)
                        }
                    }
                    .matchAny {}
                    .build()

    private fun performTransformation(dataDescriptor: DataDescriptor,
                                      targetMediaType: MediaType,
                                      parameters: Parameters): TransformedDataDescriptor {
        val (transformedDataDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            val transformer = determineTransformer(dataDescriptor, targetMediaType, parameters) ?: throw createException()

            val transformedDataDescriptor = transformer.transform(dataDescriptor, targetMediaType, parameters)

            internalCommunicationConverter.convert(dataDescriptor, transformedDataDescriptor)
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

    private fun determineTransformer(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): Transformer? =
            transformers.firstOrNull { it.canTransform(dataDescriptor, targetMediaType, parameters) }

    private fun processException(exception: Exception, parameters: Parameters): Exception =
            when (exception) {
                is TimeoutException -> TransformerTimeoutException("Couldn't transform because the transformer <$transformerId> timeout <${parameters.getTimeoutOrInfiniteIfNotFound()}> has been reached")
                else                -> exception
            }

    private fun createException(): TransformerCanNotTransformException =
            TransformerCanNotTransformException("There is no <$transformerId> transformer that can transform it. " +
                                                "The following <${transformers.size}> transformers are available: " +
                                                "<${transformers.joinToString(", ") { it.javaClass.canonicalName }}>")
}