package pl.beone.promena.core.usecase.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.SmallestMailboxPool
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import com.typesafe.config.ConfigFactory
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.communication.memory.internal.MemoryCommunicationValidatorConverter
import pl.beone.promena.communication.memory.internal.MemoryIncomingCommunicationConverter
import pl.beone.promena.communication.memory.internal.MemoryInternalCommunicationConverter
import pl.beone.promena.communication.memory.internal.MemoryOutgoingCommunicationConverter
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.external.akka.actor.DefaultActorService
import pl.beone.promena.core.external.akka.transformer.AkkaTransformerService
import pl.beone.promena.core.external.akka.transformer.config.DefaultTransformersCreator
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DefaultTransformationUseCaseTest {

    companion object {
        private const val THREADS = 4
        private const val TRANSFORMER_ACTORS = 4
        private const val NR_OF_ITERATIONS = 20

        private const val transformerId = "mirror"
        private val mediaType = MediaTypeConstants.TEXT_PLAIN
        private val targetMediaType = MediaTypeConstants.TEXT_PLAIN
        private val parameters = MapParameters.empty()
        private val communicationParameters = MapCommunicationParameters.empty()

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

        val data = InMemoryData(readFromResources().readBytes())
        transform(transformationUseCase, data)
                .forEach {
                    it shouldHaveSize 1
                    data.getBytes() shouldBe it.first().data.getBytes()
                }
    }

    private fun transform(transformationUseCase: DefaultTransformationUseCase, data: InMemoryData): List<List<TransformedDataDescriptor>> {
        val executors = Executors.newFixedThreadPool(THREADS)

        return (0..NR_OF_ITERATIONS).map {
            executors.submit(Callable<List<TransformedDataDescriptor>> {
                transformationUseCase.transform(transformerId,
                                                TransformationDescriptor(listOf(DataDescriptor(data, mediaType)), targetMediaType, parameters),
                                                communicationParameters)
            })
        }.map { it.get() }
    }

    private fun init(): DefaultTransformationUseCase {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val internalCommunicationConverter = MemoryInternalCommunicationConverter()

        val mirrorTransformer = MirrorTransformer()

        val transformerConfig = mockk<TransformerConfig> {
            every { getTransformationId(mirrorTransformer) } returns transformerId
            every { getActors(mirrorTransformer) } returns TRANSFORMER_ACTORS
            every { getPriority(mirrorTransformer) } returns 1
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

        val actorService = DefaultActorService(mirrorActorRefWithId, mockk())

        val transformerService = AkkaTransformerService(actorMaterializer, actorService)

        return DefaultTransformationUseCase(MemoryCommunicationValidatorConverter(),
                                            MemoryIncomingCommunicationConverter(),
                                            transformerService,
                                            MemoryOutgoingCommunicationConverter())
    }

    private fun readFromResources(): InputStream =
            this::class.java.getResourceAsStream("/file/test.txt")

}