package pl.beone.promena.alfresco.module.connector.activemq.internal

import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.jupiter.api.Test
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import java.time.Duration

class TransformationParametersSerializationServiceTest {

    companion object {
        private val serializationService =
            TransformationParametersSerializationService(KryoSerializationService())
    }

    @Test
    fun `serialize and deserialize`() {
        val transformationParameters = TransformationParameters(
            listOf(
                NodeRef("workspace://SpacesStore/b0bfb14c-be38-48be-90c3-cae4a7fd0c8f").toNodeDescriptor(emptyMetadata()),
                NodeRef("workspace://SpacesStore/7abdf1e2-92f4-47b2-983a-611e42f3555c").toNodeDescriptor(emptyMetadata() + ("key" to "value"))
            ),
            "123456789",
            customRetry(3, Duration.ofMillis(1000)),
            0,
            "admin"
        )

        serializationService.deserialize(
            serializationService.serialize(transformationParameters)
        ) shouldBe transformationParameters
    }
}