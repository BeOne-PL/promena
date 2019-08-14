package pl.beone.promena.core.internal.serialization

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
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

        uri shouldBe
                serializationService.deserialize(serializationService.serialize(uri), URI::class.java)
    }

    @Test
    fun `serialize and deserialize _ list of TransformedDataDescriptor`() {
        val transformedDataDescriptor =
            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value")) +
                    singleTransformedDataDescriptor("""{ "key": "value" }""".toMemoryData(), emptyMetadata())

        serializationService.deserialize(serializationService.serialize(transformedDataDescriptor), getClazz<TransformationDescriptor>()) shouldBe
                transformedDataDescriptor
    }

    @Test
    fun `serialize and deserialize _ single TransformationDescriptor`() {
        val transformationDescriptor = transformationDescriptor(
            singleTransformation("test", APPLICATION_PDF, emptyParameters()),
            singleDataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata() + ("key" to "value")) +
                    singleDataDescriptor("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata())
        )

        serializationService.deserialize(serializationService.serialize(transformationDescriptor), getClazz<TransformationDescriptor>()) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ composite TransformationDescriptor`() {
        val transformationDescriptor = transformationDescriptor(
            singleTransformation("test", APPLICATION_PDF, emptyParameters()) next
                    singleTransformation("test2", APPLICATION_OCTET_STREAM, emptyParameters()),
            singleDataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata() + ("key" to "value")) +
                    singleDataDescriptor("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata())
        )

        serializationService.deserialize(serializationService.serialize(transformationDescriptor), getClazz<TransformationDescriptor>()) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ stress test`() {
        val executor = Executors.newFixedThreadPool(4)

        (1..10).map {
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
}