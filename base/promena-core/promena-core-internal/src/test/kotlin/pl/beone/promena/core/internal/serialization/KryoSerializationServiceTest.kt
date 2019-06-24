package pl.beone.promena.core.internal.serialization

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
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

        uri shouldBe serializationService.deserialize(serializationService.serialize(uri), URI::class.java)
    }

    @Test
    fun `serialize and deserialize _ list of TransformedDataDescriptor`() {
        val transformedDataDescriptors = listOf(
                TransformedDataDescriptor(InMemoryData("test".toByteArray()), MapMetadata(mapOf("key" to "value"))),
                TransformedDataDescriptor(InMemoryData("""{ "key": "value" """.toByteArray()), MapMetadata(emptyMap()))
        )

        serializationService.deserialize(serializationService.serialize(transformedDataDescriptors), getClazz<TransformationDescriptor>()) shouldBe
                transformedDataDescriptors
    }

    @Test
    fun `serialize and deserialize _ TransformationDescriptor`() {
        val transformationDescriptor = TransformationDescriptor(
                listOf(DataDescriptor(InMemoryData("test".toByteArray()), MediaTypeConstants.APPLICATION_OCTET_STREAM),
                       DataDescriptor(InMemoryData("""{ "key": "value" }""".toByteArray()), MediaTypeConstants.APPLICATION_OCTET_STREAM)),
                MediaTypeConstants.APPLICATION_PDF,
                MapParameters(mapOf("key" to "value"))
        )

        serializationService.deserialize(serializationService.serialize(transformationDescriptor), getClazz<TransformationDescriptor>()) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ stress test`() {
        val executor = Executors.newFixedThreadPool(4)

        (1..100).map {
            executor.submit(Callable<String> {
                serializationService.deserialize(serializationService.serialize("test"), String::class.java)
            })
        }
                .map { it.get() }
                .forEach { it shouldBe "test" }
    }

    @Test
    fun `deserialize _ incorrect serialization data _ should throw DeserializationException`() {
        shouldThrow<DeserializationException> {
            serializationService.deserialize("incorrect data".toByteArray(), getClazz<String>())
        }.message shouldBe "Couldn't deserialize"
    }

    private inline fun <reified T : Any> getClazz(): Class<T> =
            T::class.java
}