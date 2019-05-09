package pl.beone.promena.core.internal.serialization

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.common.utils.getClazz
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class KryoSerializationServiceTest {

    companion object {
        private val serializationService = KryoSerializationService()
    }

    @Test
    fun `serialize and deserialize _ URI`() {
        val uri = URI("file:/tmp/tomcat.7182112197177744335.8010/")
        val afterSerialization = serializationService.deserialize(
                serializationService.serialize(uri), URI::class.java)

        assertThat(uri).isEqualTo(afterSerialization)
    }

    @Test
    fun `serialize and deserialize _ list of TransformedDataDescriptor`() {
        val transformedDataDescriptors = listOf(
                TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata(mapOf("key" to "value"))),
                TransformedDataDescriptor(InMemoryData("""{ "key": "value" """.toByteArray()), MapMetadata(emptyMap()))
        )

        assertThat(serializationService.deserialize(
                serializationService.serialize(transformedDataDescriptors),
                getClazz<TransformationDescriptor>()))
                .isEqualTo(transformedDataDescriptors)
    }

    @Test
    fun `serialize and deserialize _ TransformationDescriptor`() {
        val transformationDescriptor = TransformationDescriptor(
                listOf(DataDescriptor(InMemoryData("test".toByteArray()),
                                      MediaType.create("application/octet-stream", Charsets.UTF_8)),
                       DataDescriptor(InMemoryData("""{ "key": "value" }""".toByteArray()),
                                      MediaType.create("application/octet-stream", Charsets.UTF_8))),
                MediaTypeConstants.APPLICATION_PDF,
                MapParameters(mapOf("key" to "value"))
        )

        assertThat(serializationService.deserialize(
                serializationService.serialize(transformationDescriptor),
                getClazz<TransformationDescriptor>()))
                .isEqualTo(transformationDescriptor)
    }

    @Test
    fun `serialize and deserialize _ stress test`() {
        val executor = Executors.newFixedThreadPool(4)

        (1..100).map {
            executor.submit(Callable<String> {
                serializationService.deserialize(
                        serializationService.serialize("test"), String::class.java)
            })
        }
                .map { it.get() }
                .forEach { assertThat(it).isEqualTo("test") }
    }

    @Test
    fun `deserialize _ incorrect serialization data _ should throw DeserializationException`() {
        assertThatThrownBy { serializationService.deserialize("incorrect data".toByteArray(),
                                                                                                                                                  getClazz<String>()) }
                .isExactlyInstanceOf(DeserializationException::class.java)
                .hasMessage("Couldn't deserialize")
    }
}