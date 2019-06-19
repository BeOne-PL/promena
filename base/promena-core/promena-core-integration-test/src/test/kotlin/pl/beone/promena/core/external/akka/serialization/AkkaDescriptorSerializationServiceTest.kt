package pl.beone.promena.core.external.akka.serialization

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.common.utils.getClazz
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.SerializerActor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

class AkkaDescriptorSerializationServiceTest {

    companion object {
        private val data = InMemoryData("test".toByteArray())
        private val data2 = InMemoryData("test2".toByteArray())
        private val serializedTransformedDataDescriptors = "serialized data".toByteArray()

    }

    private lateinit var actorSystem: ActorSystem

    @Before
    fun setUp() {
        actorSystem = ActorSystem.create()
    }

    @After
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun serialize() {
        val transformedDataDescriptors = listOf(
                TransformedDataDescriptor(data, MapMetadata(mapOf("key" to "value"))),
                TransformedDataDescriptor(data2, MapMetadata(emptyMap()))
        )

        val dataDescriptorSerializationService = prepare(mockk {
            every { serialize(transformedDataDescriptors) } returns serializedTransformedDataDescriptors
        })

        dataDescriptorSerializationService.serialize(transformedDataDescriptors) shouldBe serializedTransformedDataDescriptors
    }

    @Test
    fun deserialize() {
        val transformationDescriptor = TransformationDescriptor(
                listOf(DataDescriptor(data, MediaTypeConstants.APPLICATION_OCTET_STREAM),
                       DataDescriptor(data2, MediaTypeConstants.APPLICATION_OCTET_STREAM)),
                MediaTypeConstants.APPLICATION_PDF,
                MapParameters(mapOf("key" to "value"))
        )

        val dataDescriptorSerializationService = prepare(mockk {
            every { deserialize(serializedTransformedDataDescriptors, getClazz<TransformationDescriptor>()) } returns transformationDescriptor
        })

        dataDescriptorSerializationService.deserialize(serializedTransformedDataDescriptors) shouldBe transformationDescriptor
    }

    @Test
    fun `deserialize _ should throw DeserializationException`() {
        val incorrectSerializedTransformedDataDescriptors = "incorrect serialized data".toByteArray()

        val dataDescriptorSerializationService = prepare(mockk {
            every {
                deserialize(incorrectSerializedTransformedDataDescriptors, getClazz<TransformationDescriptor>())
            } throws DeserializationException("")
        })

        shouldThrow<DeserializationException> { dataDescriptorSerializationService.deserialize(incorrectSerializedTransformedDataDescriptors) }
    }

    private fun prepare(serializationService: SerializationService): AkkaDescriptorSerializationService {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val serializerActor = actorSystem.actorOf(Props.create { SerializerActor(serializationService) }, SerializerActor.actorName)

        val actorService = mockk<ActorService> {
            every { getSerializerActor() } returns serializerActor
        }

        return AkkaDescriptorSerializationService(actorMaterializer, actorService)
    }

}