@file:Suppress("UNCHECKED_CAST")

package pl.beone.promena.core.external.akka.serialization

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.message.DeserializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.SerializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToDeserializeMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToSerializeMessage
import pl.beone.promena.core.external.akka.util.infiniteTimeout
import pl.beone.promena.core.external.akka.util.unwrapExecutionException

class AkkaSerializationService(
    private val actorMaterializer: ActorMaterializer,
    private val actorRef: ActorRef
) : SerializationService {

    override fun <T> serialize(element: T): ByteArray =
        finishFlow(
            createSource(element as Any)
                .via(createSerializeFlow())
        )

    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
        finishFlow(
            createSource(bytes)
                .via(createDeserializeFlow(clazz))
        )

    private fun <T> finishFlow(flow: Source<T, NotUsed>): T =
        unwrapExecutionException {
            flow.runWith(Sink.head(), actorMaterializer)
                .toCompletableFuture()
                .get()
        }

    private fun <T> createSource(element: T): Source<T, NotUsed> =
        Source.single(element)

    private fun createSerializeFlow(): Flow<Any, ByteArray, NotUsed> =
        Flow.of(Any::class.java)
            .map(::ToSerializeMessage)
            .ask(actorRef, SerializedMessage::class.java, infiniteTimeout)
            .map(SerializedMessage::bytes)

    private fun <T> createDeserializeFlow(clazz: Class<T>): Flow<ByteArray, T, NotUsed> =
        Flow.of(getClazz<ByteArray>())
            .map { bytes -> ToDeserializeMessage(bytes, clazz) }
            .ask(actorRef, DeserializedMessage::class.java, infiniteTimeout)
            .map { (element) -> element as T }
}