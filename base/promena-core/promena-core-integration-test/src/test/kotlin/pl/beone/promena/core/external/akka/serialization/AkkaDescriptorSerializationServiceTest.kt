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
import pl.beone.promena.transformer.internal.data.and
import pl.beone.promena.transformer.internal.data.dataDescriptor
import pl.beone.promena.transformer.internal.data.transformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.add
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.metadata
import pl.beone.promena.transformer.internal.model.parameters.add
import pl.beone.promena.transformer.internal.model.parameters.parameters
import pl.beone.promena.transformer.internal.transformation.transformationFlow

class AkkaDescriptorSerializationServiceTest {

    companion object {
        private val data = "test".toMemoryData()
        private val data2 = "test2".toMemoryData()
        private val metadata = metadata() add ("key" to "value")
        private val metadata2 = emptyMetadata()
        private val serializedTransformedDataDescriptors = "serialized data".toByteArray()
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
        val transformedDataDescriptors = transformedDataDescriptor(data, metadata)
                .and(data2, metadata2)

        val serializationService = prepare(mockk {
            every { serialize(transformedDataDescriptors) } returns serializedTransformedDataDescriptors
        })

        serializationService.serialize(transformedDataDescriptors) shouldBe serializedTransformedDataDescriptors
    }

    @Test
    fun deserialize() {
        val transformationDescriptor = TransformationDescriptor.of(
                transformationFlow("test", MediaTypeConstants.APPLICATION_PDF, parameters() add ("key" to "value")),
                dataDescriptor(data, APPLICATION_OCTET_STREAM, metadata)
                        .and(data2, APPLICATION_OCTET_STREAM, metadata2)
        )

        val serializationService = prepare(mockk {
            every { deserialize(serializedTransformedDataDescriptors, getClazz<TransformationDescriptor>()) } returns transformationDescriptor
        })

        serializationService.deserialize(serializedTransformedDataDescriptors) shouldBe transformationDescriptor
    }

    @Test
    fun `deserialize _ should throw DeserializationException`() {
        val incorrectSerializedTransformedDataDescriptors = "incorrect serialized data".toByteArray()

        val serializationService = prepare(mockk {
            every {
                deserialize(incorrectSerializedTransformedDataDescriptors, getClazz<TransformationDescriptor>())
            } throws DeserializationException("")
        })

        shouldThrow<DeserializationException> { serializationService.deserialize(incorrectSerializedTransformedDataDescriptors) }
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