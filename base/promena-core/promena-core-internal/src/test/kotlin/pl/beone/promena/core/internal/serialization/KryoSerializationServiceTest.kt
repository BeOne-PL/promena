package pl.beone.promena.core.internal.serialization

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.internal.data.and
import pl.beone.promena.transformer.internal.data.dataDescriptor
import pl.beone.promena.transformer.internal.data.transformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.add
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.metadata
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.transformation.next
import pl.beone.promena.transformer.internal.transformation.transformationFlow
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
        val transformedDataDescriptors =
                transformedDataDescriptor("test".toMemoryData(), metadata() add ("key" to "value"))
                        .and("""{ "key": "value" }""".toMemoryData(), emptyMetadata())

        serializationService.deserialize(serializationService.serialize(transformedDataDescriptors), getClazz<TransformationDescriptor>()) shouldBe
                transformedDataDescriptors
    }

    @Test
    fun `serialize and deserialize _ TransformationDescriptor with single flow`() {
        val transformationDescriptor = TransformationDescriptor.of(
                transformationFlow("test", APPLICATION_PDF, emptyParameters()),
                dataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, metadata() add ("key" to "value"))
                        .and("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata())
        )

        serializationService.deserialize(serializationService.serialize(transformationDescriptor), getClazz<TransformationDescriptor>()) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ TransformationDescriptor with composite flow`() {
        val transformationDescriptor = TransformationDescriptor.of(
                transformationFlow("test", APPLICATION_PDF, emptyParameters())
                        .next("test2", APPLICATION_OCTET_STREAM, emptyParameters()),
                dataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, metadata() add ("key" to "value"))
                        .and("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata())
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