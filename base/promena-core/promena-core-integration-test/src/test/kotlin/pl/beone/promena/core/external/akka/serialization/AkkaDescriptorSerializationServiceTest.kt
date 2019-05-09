package pl.beone.promena.core.external.akka.serialization

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.common.utils.getClazz
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.SerializerActor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

class AkkaDescriptorSerializationServiceTest {

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
                TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata(mapOf("key" to "value"))),
                TransformedDataDescriptor(InMemoryData("test2".toByteArray()), MapMetadata(emptyMap()))
        )

        val dataDescriptorSerializationService = prepare(mock {
            on { serialize(transformedDataDescriptors) } doReturn "serialized data".toByteArray()
        })

        assertThat(dataDescriptorSerializationService.serialize(transformedDataDescriptors))
                .isEqualTo("serialized data".toByteArray())
    }

    @Test
    fun deserialize() {
        val transformationDescriptor = TransformationDescriptor(
                listOf(DataDescriptor(InMemoryData("test".toByteArray()),
                                      MediaType.create("application/octet-stream", Charsets.UTF_8)),
                       DataDescriptor(InMemoryData("test2".toByteArray()),
                                      MediaType.create("application/octet-stream", Charsets.UTF_8))),
                MediaTypeConstants.APPLICATION_PDF,
                MapParameters(mapOf("key" to "value"))
        )

        val dataDescriptorSerializationService = prepare(mock {
            on { deserialize("serialized data".toByteArray(), getClazz<TransformationDescriptor>()) } doReturn transformationDescriptor
        })

        assertThat(dataDescriptorSerializationService.deserialize("serialized data".toByteArray()))
                .isEqualTo(transformationDescriptor)
    }

    @Test
    fun `deserialize _ should throw DeserializationException`() {
        val dataDescriptorSerializationService = prepare(mock {
            on { deserialize("incorrect serialized data".toByteArray(), getClazz<TransformationDescriptor>()) } doThrow DeserializationException("")
        })

        assertThatThrownBy { dataDescriptorSerializationService.deserialize("incorrect serialized data".toByteArray()) }
                .isExactlyInstanceOf(DeserializationException::class.java)
    }

    private fun prepare(serializationService: SerializationService): AkkaDescriptorSerializationService {
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val serializerActor =
                actorSystem.actorOf(Props.create(SerializerActor::class.java, serializationService), "serializer")

        val actorService = mock<ActorService> {
            on { getSerializerActor() } doReturn serializerActor
        }

        return AkkaDescriptorSerializationService(actorMaterializer, actorService)
    }

}