package pl.beone.promena.core.external.akka.actor.serializer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.common.utils.*
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.message.DeserializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.SerializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToDeserializeMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToSerializeMessage
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class SerializerActor(private val serializationService: SerializationService) : AbstractLoggingActor() {

    override fun createReceive() =
            receiveBuilder()
                    .match(ToSerializeMessage::class.java) {
                        try {
                            sender.tell(SerializedMessage(serialize(it.transformedDataDescriptors)), self)
                        } catch (e: Exception) {
                            sender.tell(Status.Failure(e), self)
                        }
                    }
                    .match(ToDeserializeMessage::class.java) {
                        try {
                            sender.tell(DeserializedMessage(deserialize(it.bytes)), self)
                        } catch (e: Exception) {
                            sender.tell(Status.Failure(e), self)
                        }
                    }
                    .matchAny {}
                    .build()!!

    private fun serialize(transformedDataDescriptors: List<TransformedDataDescriptor>): ByteArray {
        val (bytes, measuredTimeMs) = measureTimeMillisWithContent {
            serializationService.serialize(transformedDataDescriptors)
        }

        if (log().isDebugEnabled) {
            log().debug("Serialized to <{} MB> in <{} s>",
                        bytes.toMB().format(2),
                        measuredTimeMs.toSeconds())
        }

        return bytes
    }

    private fun deserialize(bytes: ByteArray): TransformationDescriptor {
        val (transformationDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            serializationService.deserialize<TransformationDescriptor>(bytes, getClazz())
        }

        if (log().isDebugEnabled) {
            log().debug("Deserialized from <{} MB> in <{} s>",
                        bytes.toMB().format(2),
                        measuredTimeMs.toSeconds())
        }

        return transformationDescriptor
    }

}
