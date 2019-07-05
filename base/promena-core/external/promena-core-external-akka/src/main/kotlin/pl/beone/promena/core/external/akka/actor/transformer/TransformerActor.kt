package pl.beone.promena.core.external.akka.actor.transformer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.util.format
import pl.beone.promena.core.external.akka.util.measureTimeMillisWithContent
import pl.beone.promena.core.external.akka.util.toMB
import pl.beone.promena.core.external.akka.util.toSeconds
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class TransformerActor(private val transformers: List<Transformer>,
                       private val internalTransformedDataDescriptorConverter: InternalCommunicationConverter) : AbstractLoggingActor() {

    override fun createReceive(): Receive =
            receiveBuilder()
                    .match(ToTransformMessage::class.java) {
                        try {
                            val dataDescriptors =
                                    transform(it.dataDescriptors, it.targetMediaType, it.parameters)

                            sender.tell(TransformedMessage(dataDescriptors), self)
                        } catch (e: Exception) {
                            sender.tell(Status.Failure(e), self)
                        }
                    }
                    .matchAny {}
                    .build()

    private fun transform(dataDescriptors: List<DataDescriptor>,
                          targetMediaType: MediaType,
                          parameters: Parameters): List<TransformedDataDescriptor> {
        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            val transformer = transformers.firstOrNull { it.canTransform(dataDescriptors, targetMediaType, parameters) }
                              ?: throw TransformerNotFoundException("There is no transformer that can process it. " +
                                                                    "There following <${transformers.size}> transformers are available: " +
                                                                    "<${transformers.joinToString(", ") { it.javaClass.canonicalName }}>")

            val transformedDataDescriptors = transformer.transform(dataDescriptors, targetMediaType, parameters)

            internalTransformedDataDescriptorConverter.convert(dataDescriptors, transformedDataDescriptors)
        }

        if (log().isDebugEnabled) {
            // I have to use replace functions because Akka debug handles only 4 arguments
            log().debug("Transformed <:1, :2> from <:3 MB, :4 source(s)> to <:5 MB, :6 result(s)> in <:7 s>"
                                .replace(":1", targetMediaType.toString())
                                .replace(":2", parameters.toString())
                                .replace(":3", dataDescriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":4", dataDescriptors.size.toString())
                                .replace(":5", transformedDataDescriptors.map { it.data.getBytes() }.toMB().format(2))
                                .replace(":6", transformedDataDescriptors.size.toString())
                                .replace(":7", measuredTimeMs.toSeconds().toString()))
        }

        return transformedDataDescriptors
    }
}