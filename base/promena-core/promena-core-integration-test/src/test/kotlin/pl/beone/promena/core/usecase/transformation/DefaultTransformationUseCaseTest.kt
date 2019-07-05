package pl.beone.promena.core.usecase.transformation

import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.SmallestMailboxPool
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import com.typesafe.config.ConfigFactory
import io.kotlintest.fail
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.external.IncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.OutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunicationManager
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
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
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class DefaultTransformationUseCaseTest {

    companion object {
        private const val TRANSFORMER_ACTORS = 1

        private const val transformerId = "mirror"
        private const val communicationId = "memory"
        private val data = InMemoryData(this::class.java.getResourceAsStream("/file/test.txt").readBytes())
        private val mediaType = MediaTypeConstants.TEXT_PLAIN
        private val targetMediaType = MediaTypeConstants.TEXT_PLAIN
        private val parameters = MapParameters.empty()
        private val dataDescriptors = listOf(DataDescriptor(data, mediaType))
        private val transformationDescriptor = TransformationDescriptor(dataDescriptors, targetMediaType, parameters)
        private val transformedDataDescriptor = listOf(TransformedDataDescriptor(data, MapMetadata.empty()))
        private val externalCommunicationParameters = MapCommunicationParameters.create(communicationId)
        private val internalCommunicationParameters = MapCommunicationParameters.create(communicationId)
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

        try {
            transformationUseCase.transform(transformerId, transformationDescriptor, externalCommunicationParameters).let {
                it shouldHaveSize 1
                data.getBytes() shouldBe it.first().data.getBytes()

            }
        } catch (e: Exception) {
            fail("Error occurred. Check logs for more details")
        }
    }

    private fun init(): DefaultTransformationUseCase {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val incomingExternalCommunicationConverter = mockk<IncomingExternalCommunicationConverter> {
            every { convert(dataDescriptors, externalCommunicationParameters, internalCommunicationParameters) } returns dataDescriptors
        }

        val internalCommunicationConverter = mockk<InternalCommunicationConverter> {
            every { convert(dataDescriptors, transformedDataDescriptor) } returns transformedDataDescriptor
        }

        val outgoingExternalCommunicationConverter = mockk<OutgoingExternalCommunicationConverter> {
            every {
                convert(transformedDataDescriptor, externalCommunicationParameters, internalCommunicationParameters)
            } returns transformedDataDescriptor
        }

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

        val externalCommunicationManager = mockk<ExternalCommunicationManager> {
            every { getCommunication(communicationId) } returns ExternalCommunication(communicationId,
                                                                                      incomingExternalCommunicationConverter,
                                                                                      outgoingExternalCommunicationConverter)
        }

        val transformersCreator = DefaultTransformersCreator(transformerConfig,
                                                             internalCommunicationConverter,
                                                             actorCreator)

        val mirrorActorRefWithId = transformersCreator.create(listOf(mirrorTransformer))

        val actorService = DefaultActorService(mirrorActorRefWithId, mockk())

        val transformerService = AkkaTransformerService(actorMaterializer, actorService)

        return DefaultTransformationUseCase(externalCommunicationManager, internalCommunicationParameters, transformerService)
    }

}