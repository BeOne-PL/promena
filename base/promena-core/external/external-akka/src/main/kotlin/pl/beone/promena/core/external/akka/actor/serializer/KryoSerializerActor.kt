package pl.beone.promena.core.external.akka.actor.serializer

import akka.actor.AbstractLoggingActor
import akka.actor.Status
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.message.DeserializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.SerializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToDeserializeMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToSerializeMessage
import pl.beone.promena.core.external.akka.util.measureTimeMillisWithContent
import pl.beone.promena.transformer.internal.extension.format
import pl.beone.promena.transformer.internal.extension.toMB
import pl.beone.promena.transformer.internal.extension.toSeconds

/**
 * This implementation of an Akka actor uses [serializationService] to serialize and deserialize objects.
 */
class KryoSerializerActor(
    private val serializationService: SerializationService
) : AbstractLoggingActor() {

    companion object {
        const val actorName = "serializer"
    }

    override fun createReceive() =
        receiveBuilder()
            .match(ToSerializeMessage::class.java) { (element) ->
                try {
                    sender.tell(SerializedMessage(serialize(element)), self)
                } catch (e: Exception) {
                    sender.tell(Status.Failure(e), self)
                }
            }
            .match(ToDeserializeMessage::class.java) { (bytes, clazz) ->
                try {
                    sender.tell(DeserializedMessage(deserialize(bytes, clazz)), self)
                } catch (e: Exception) {
                    sender.tell(Status.Failure(e), self)
                }
            }
            .matchAny {}
            .build()!!

    private fun serialize(element: Any): ByteArray {
        val (bytes, measuredTimeMs) = measureTimeMillisWithContent {
            serializationService.serialize(element)
        }

        if (log().isDebugEnabled) {
            log().debug(
                "Serialized to <{} MB> in <{} s>",
                bytes.toMB().format(2),
                measuredTimeMs.toSeconds().format(3)
            )
        }

        return bytes
    }

    private fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T {
        val (transformationDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            serializationService.deserialize(bytes, clazz)
        }

        if (log().isDebugEnabled) {
            log().debug(
                "Deserialized from <{} MB> in <{} s>",
                bytes.toMB().format(2),
                measuredTimeMs.toSeconds().format(3)
            )
        }

        return transformationDescriptor
    }
}
