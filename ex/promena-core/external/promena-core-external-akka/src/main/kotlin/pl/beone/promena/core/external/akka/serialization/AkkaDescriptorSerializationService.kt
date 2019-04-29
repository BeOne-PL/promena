package pl.beone.promena.core.external.akka.serialization

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.util.Timeout
import pl.beone.promena.core.common.utils.getClazz
import pl.beone.promena.core.common.utils.infiniteDuration
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.external.akka.actor.serializer.message.DeserializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.SerializedMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToDeserializeMessage
import pl.beone.promena.core.external.akka.actor.serializer.message.ToSerializeMessage
import pl.beone.promena.core.common.utils.unwrapExecutionException
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class AkkaDescriptorSerializationService(private val actorMaterializer: ActorMaterializer,
                                         private val actorService: ActorService) : DescriptorSerializationService {

    companion object {
        private val infiniteTimeout = Timeout.create(infiniteDuration)
    }

    override fun serialize(transformedDataDescriptors: List<TransformedDataDescriptor>): ByteArray =
            finishFlow(createSource(transformedDataDescriptors)
                               .via(createSerializeFlow()))

    override fun deserialize(bytes: ByteArray): TransformationDescriptor =
            finishFlow(createSource(bytes)
                               .via(createDeserializeFlow()))

    private fun <T> finishFlow(flow: Source<T, NotUsed>): T =
            unwrapExecutionException {
                flow.runWith(Sink.head(), actorMaterializer)
                        .toCompletableFuture()
                        .get()
            }

    private fun <T> createSource(element: T): Source<T, NotUsed> =
            Source.single(element)

    private fun createSerializeFlow(): Flow<List<TransformedDataDescriptor>, ByteArray, NotUsed> =
            Flow.of(getClazz<List<TransformedDataDescriptor>>())
                    .map { ToSerializeMessage(it) }
                    .ask(actorService.getSerializerActor(), SerializedMessage::class.java,
                         infiniteTimeout)
                    .map { it.bytes }

    private fun createDeserializeFlow(): Flow<ByteArray, TransformationDescriptor, NotUsed> =
            Flow.of(getClazz<ByteArray>())
                    .map { ToDeserializeMessage(it) }
                    .ask(actorService.getSerializerActor(), DeserializedMessage::class.java,
                         infiniteTimeout)
                    .map { it.transformationDescriptor }
}