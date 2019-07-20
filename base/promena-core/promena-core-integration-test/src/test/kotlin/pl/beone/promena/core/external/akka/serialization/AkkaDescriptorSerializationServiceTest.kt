package pl.beone.promena.core.external.akka.serialization

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.SerializerActor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus

class AkkaDescriptorSerializationServiceTest {

    companion object {
        private val data = "test".toMemoryData()
        private val data2 = "test2".toMemoryData()
        private val metadata = emptyMetadata() + ("key" to "value")
        private val metadata2 = emptyMetadata()
        private val serializedTransformedDataDescriptor = "serialized data".toByteArray()
    }

    private lateinit var actorSystem: ActorSystem

    @Before
    fun setUp() {
        actorSystem = ActorSystem.create()

        (LoggerFactory.getLogger("pl.beone.promena.core.external.akka.transformer.AkkaTransformerService") as Logger).level = Level.DEBUG
    }

    @After
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun serialize() {
        val transformedDataDescriptor = singleTransformedDataDescriptor(data, metadata) +
                singleTransformedDataDescriptor(data2, metadata2)

        val serializationService = prepare(mockk {
            every { serialize(transformedDataDescriptor) } returns serializedTransformedDataDescriptor
        })

        serializationService.serialize(transformedDataDescriptor) shouldBe serializedTransformedDataDescriptor
    }

    @Test
    fun deserialize() {
        val transformationDescriptor = TransformationDescriptor.of(
                singleTransformation("test", MediaTypeConstants.APPLICATION_PDF, emptyParameters() + ("key" to "value")),
                singleDataDescriptor(data, APPLICATION_OCTET_STREAM, metadata) +
                        singleDataDescriptor(data2, APPLICATION_OCTET_STREAM, metadata2)
        )

        val serializationService = prepare(mockk {
            every { deserialize(serializedTransformedDataDescriptor, getClazz<TransformationDescriptor>()) } returns transformationDescriptor
        })

        serializationService.deserialize(serializedTransformedDataDescriptor) shouldBe transformationDescriptor
    }

    @Test
    fun `deserialize _ should throw DeserializationException`() {
        val incorrectSerializedTransformedDataDescriptor = "incorrect serialized data".toByteArray()

        val serializationService = prepare(mockk {
            every {
                deserialize(incorrectSerializedTransformedDataDescriptor, getClazz<TransformationDescriptor>())
            } throws DeserializationException("")
        })

        shouldThrow<DeserializationException> { serializationService.deserialize(incorrectSerializedTransformedDataDescriptor) }
    }

    private fun prepare(serializationService: SerializationService): AkkaDescriptorSerializationService {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val serializerActor =
                actorSystem.actorOf(Props.create(SerializerActor::class.java) { SerializerActor(serializationService) }, SerializerActor.actorName)

        val actorService = mockk<ActorService> {
            every { getSerializerActor() } returns serializerActor
        }

        return AkkaDescriptorSerializationService(actorMaterializer, actorService)
    }

}