package pl.beone.promena.core.usecase.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.SmallestMailboxPool
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.typesafe.config.ConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.communication.memory.internal.MemoryCommunicationValidatorConverter
import pl.beone.promena.communication.memory.internal.MemoryIncomingCommunicationConverter
import pl.beone.promena.communication.memory.internal.MemoryInternalCommunicationConverter
import pl.beone.promena.communication.memory.internal.MemoryOutgoingCommunicationConverter
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.common.utils.getClazz
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.external.akka.actor.DefaultActorService
import pl.beone.promena.core.external.akka.actor.serializer.SerializerActor
import pl.beone.promena.core.external.akka.serialization.AkkaDescriptorSerializationService
import pl.beone.promena.core.external.akka.transformer.AkkaTransformerService
import pl.beone.promena.core.external.akka.transformer.config.DefaultTransformersCreator
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DefaultTransformationUseCaseTest {

    companion object {
        private const val THREADS = 4
        private const val TRANSFORMER_ACTORS = 4
        private const val SERIALIZER_ACTORS = 2
        private const val NR_OF_ITERATIONS = 20

        private val serializationService = KryoSerializationService()
    }

    private lateinit var actorSystem: ActorSystem

    @Before
    fun setUp() {
        actorSystem = ActorSystem.create("test", ConfigFactory.load("resource-test.conf"))
    }

    @After
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun `transform _ run Akka and transform using memory communication and simple data classes _ should perform transformation`() {
        val transformationUseCase = init()

        val data = InMemoryData(this::class.java.getResourceAsStream("/file/test.txt").readBytes())
        transform(transformationUseCase, data)
                .forEach {
                    assertThat(it).hasSize(1)
                    assertThat(data.getBytes()).isEqualTo(it.first().data.getBytes())
                }
    }

    private fun transform(transformationUseCase: DefaultTransformationUseCase, data: InMemoryData): List<List<TransformedDataDescriptor>> {
        val executors = Executors.newFixedThreadPool(THREADS)

        return (0..NR_OF_ITERATIONS).map {
            executors.submit(Callable<List<TransformedDataDescriptor>> {
                val transformationDescriptor = TransformationDescriptor(listOf(DataDescriptor(data, MediaTypeConstants.TEXT_PLAIN)),
                                                                        MediaTypeConstants.TEXT_PLAIN,
                                                                        MapParameters.empty())

                val transformedBytes = transformationUseCase.transform("mirror",
                                                                       serializationService.serialize(transformationDescriptor),
                                                                       MapCommunicationParameters.empty())

                serializationService.deserialize(transformedBytes, getClazz<List<TransformedDataDescriptor>>())
            })
        }.map { it.get() }
    }

    private fun init(): DefaultTransformationUseCase {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val internalCommunicationConverter = MemoryInternalCommunicationConverter()

        val mirrorTransformer = MirrorTransformer()

        val transformerConfig = mock<TransformerConfig> {
            on { getTransformationId(mirrorTransformer) } doReturn "mirror"
            on { getActors(mirrorTransformer) } doReturn TRANSFORMER_ACTORS
            on { getPriority(mirrorTransformer) } doReturn 1
        }

        val actorCreator = object : ActorCreator {
            override fun create(transformerId: String, props: Props, actors: Int): ActorRefWithId {
                return ActorRefWithId(actorSystem.actorOf(SmallestMailboxPool(actors).props(props), transformerId), transformerId)
            }
        }

        val transformersCreator = DefaultTransformersCreator(transformerConfig,
                                                             internalCommunicationConverter,
                                                             actorCreator)

        val mirrorActorRefWithId = transformersCreator.create(listOf(mirrorTransformer))

        val serializerActorRef = actorCreator.create("serializer",
                                                     Props.create(SerializerActor::class.java, serializationService),
                                                     SERIALIZER_ACTORS).ref

        val actorService = DefaultActorService(mirrorActorRefWithId, serializerActorRef)

        val descriptorSerializationService = AkkaDescriptorSerializationService(actorMaterializer, actorService)

        val transformerService = AkkaTransformerService(actorMaterializer, actorService)

        return DefaultTransformationUseCase(descriptorSerializationService,
                                            MemoryCommunicationValidatorConverter(),
                                            MemoryIncomingCommunicationConverter(),
                                            transformerService,
                                            MemoryOutgoingCommunicationConverter())
    }
}