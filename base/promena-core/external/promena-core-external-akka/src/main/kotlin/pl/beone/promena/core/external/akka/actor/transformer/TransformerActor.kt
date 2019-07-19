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
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.model.Parameters
import java.util.concurrent.TimeoutException

class TransformerActor(private val transformerId: String,
                       private val transformers: List<Transformer>,
                       private val internalCommunicationConverter: InternalCommunicationConverter) : AbstractLoggingActor() {

    override fun createReceive(): Receive =
            receiveBuilder()
                    .match(ToTransformMessage::class.java) {
                        try {
                            val transformedDataDescriptors = performTransformation(it.dataDescriptors, it.targetMediaType, it.parameters)
                            sender.tell(TransformedMessage(transformedDataDescriptors), self)
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

    private fun performTransformation(dataDescriptors: DataDescriptors,
                                      targetMediaType: MediaType,
                                      parameters: Parameters): TransformedDataDescriptors {
        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            val transformer = determineTransformer(dataDescriptors, targetMediaType, parameters) ?: throw createException()

            val transformedDataDescriptors = transformer.transform(dataDescriptors, targetMediaType, parameters)

            internalCommunicationConverter.convert(dataDescriptors, transformedDataDescriptors)
        }

        if (log().isDebugEnabled) {
            // I have to use replace functions because Akka debug handles only 4 arguments
            log().debug("Transformed <:1, :2> from <:3 MB, :4 source(s)> to <:5 MB, :6 result(s)> in <:7 s>"
                                .replace(":1", targetMediaType.toString())
                                .replace(":2", parameters.toString())
                                .replace(":3", dataDescriptors.descriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":4", dataDescriptors.descriptors.size.toString())
                                .replace(":5", transformedDataDescriptors.descriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":6", transformedDataDescriptors.descriptors.size.toString())
                                .replace(":7", measuredTimeMs.toSeconds().toString()))
        }

        return transformedDataDescriptors
    }

    private fun determineTransformer(dataDescriptors: DataDescriptors, targetMediaType: MediaType, parameters: Parameters): Transformer? =
            transformers.firstOrNull { it.canTransform(dataDescriptors, targetMediaType, parameters) }

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