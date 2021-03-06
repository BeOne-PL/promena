package pl.beone.promena.core.external.akka.serialization

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import akka.testkit.javadsl.TestKit
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.performedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.core.external.akka.actor.serializer.KryoSerializerActor
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_OCTET_STREAM
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.next
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class AkkaSerializationServiceTestIT {

    private lateinit var actorSystem: ActorSystem
    private lateinit var akkaSerializationService: AkkaSerializationService

    @BeforeEach
    fun setUp() {
        actorSystem = ActorSystem.create()
        val actorMaterializer = ActorMaterializer.create(actorSystem)

        val serializerActor = actorSystem.actorOf(
            Props.create(KryoSerializerActor::class.java) { KryoSerializerActor(KryoSerializationService()) }, "serializer"
        )

        akkaSerializationService = AkkaSerializationService(actorMaterializer, serializerActor)
    }

    @AfterEach
    fun teardown() {
        TestKit.shutdownActorSystem(actorSystem)
    }

    @Test
    fun `serialize and deserialize _ URI`() {
        val uri = URI("file:/tmp/tomcat.7182112197177744335.8010/")

        uri shouldBe
                akkaSerializationService.deserialize(akkaSerializationService.serialize(uri), URI::class.java)
    }

    @Test
    fun `serialize and deserialize _ list of TransformedDataDescriptor`() {
        val transformedDataDescriptor =
            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value")) +
                    singleTransformedDataDescriptor("""{ "key": "value" }""".toMemoryData(), emptyMetadata())

        akkaSerializationService.deserialize(
            akkaSerializationService.serialize(transformedDataDescriptor),
            getClazz<TransformationDescriptor>()
        ) shouldBe
                transformedDataDescriptor
    }

    @Test
    fun `serialize and deserialize _ single TransformationDescriptor`() {
        val transformationDescriptor = transformationDescriptor(
            singleTransformation("test", APPLICATION_PDF, emptyParameters()),
            singleDataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata() + ("key" to "value")) +
                    singleDataDescriptor("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata()),
            communicationParameters("file") + ("directory" to createTempDir())
        )

        akkaSerializationService.deserialize(
            akkaSerializationService.serialize(transformationDescriptor),
            getClazz<TransformationDescriptor>()
        ) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ composite TransformationDescriptor`() {
        val transformationDescriptor = transformationDescriptor(
            singleTransformation("test", APPLICATION_PDF, emptyParameters()) next
                    singleTransformation("test2", APPLICATION_OCTET_STREAM, emptyParameters()),
            singleDataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata() + ("key" to "value")) +
                    singleDataDescriptor("""{ "key": "value" }""".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata()),
            communicationParameters("file") + ("directory" to createTempDir())
        )

        akkaSerializationService.deserialize(
            akkaSerializationService.serialize(transformationDescriptor),
            getClazz<TransformationDescriptor>()
        ) shouldBe
                transformationDescriptor
    }

    @Test
    fun `serialize and deserialize _ stress test`() {
        val executor = Executors.newFixedThreadPool(4)

        (1..10).map {
            executor.submit(Callable<String> {
                akkaSerializationService.deserialize(akkaSerializationService.serialize("test"), String::class.java)
            })
        }
            .map { it.get() }
            .forEach { it shouldBe "test" }
    }

    @Test
    fun `deserialize _ incorrect serialization data _ should throw DeserializationException`() {
        shouldThrow<DeserializationException> {
            akkaSerializationService.deserialize("incorrect data".toByteArray(), getClazz<String>())
        }.message shouldBe "Couldn't deserialize"
    }

    @Test
    fun `serialize _ and _ deserialize _ PerformedTransformationDescriptor`() {
        val transformedDataDescriptor = performedTransformationDescriptor(
            singleTransformedDataDescriptor("test".toMemoryData(), emptyMetadata() + ("key" to "value")) +
                    singleTransformedDataDescriptor("test2".toMemoryData(), emptyMetadata())
        )

        akkaSerializationService.deserialize(
            akkaSerializationService.serialize(transformedDataDescriptor),
            getClazz<PerformedTransformationDescriptor>()
        ) shouldBe
                transformedDataDescriptor
    }

    @Test
    fun `serialize _ and _ deserialize _ TransformationDescriptor`() {
        val transformationDescriptor = transformationDescriptor(
            singleTransformation("test", APPLICATION_PDF, emptyParameters() + ("key" to "value")),
            singleDataDescriptor("test".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata() + ("key" to "value")) +
                    singleDataDescriptor("test2".toMemoryData(), APPLICATION_OCTET_STREAM, emptyMetadata()),
            communicationParameters("file") + ("directory" to createTempDir())
        )

        akkaSerializationService.deserialize(
            akkaSerializationService.serialize(transformationDescriptor),
            getClazz<PerformedTransformationDescriptor>()
        ) shouldBe
                transformationDescriptor

    }

}