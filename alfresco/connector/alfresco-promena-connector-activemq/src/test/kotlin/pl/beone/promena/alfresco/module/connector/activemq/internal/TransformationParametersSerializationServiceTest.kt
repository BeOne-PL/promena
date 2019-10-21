package pl.beone.promena.alfresco.module.connector.activemq.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import org.alfresco.model.ContentModel.PROP_NAME
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.connector.activemq.AbstractUtilsAlfrescoIT
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.PostTransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.time.Duration

@RunWith(AlfrescoTestRunner::class)
class TransformationParametersSerializationServiceTest : AbstractUtilsAlfrescoIT() {

    companion object {
        private val serializationService =
            TransformationParametersSerializationService(KryoSerializationService())
    }

    @Test
    fun serializeAndDeserialize() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()
        val transformationParameters = TransformationParameters(
            NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata()),
            PostTransformationExecution { _, _, serviceRegistry, result ->
                serviceRegistry.nodeService.setProperty(result.nodeRefs[0], PROP_NAME, "changed")
            },
            customRetry(3, Duration.ofMillis(1000)),
            singleDataDescriptor("".toMemoryData(), APPLICATION_PDF, emptyMetadata()),
            "123456789",
            0,
            "admin"
        )

        serializationService.deserialize(
            serializationService.serialize(transformationParameters)
        ).let {
            it.nodeDescriptor shouldBe transformationParameters.nodeDescriptor
            it.retry shouldBe transformationParameters.retry
            it.dataDescriptor shouldBe transformationParameters.dataDescriptor
            it.nodesChecksum shouldBe transformationParameters.nodesChecksum
            it.attempt shouldBe transformationParameters.attempt
            it.userName shouldBe transformationParameters.userName

            it.postTransformationExecution shouldNotBe null
            it.postTransformationExecution!!.execute(mockk(), mockk(), serviceRegistry, transformationExecutionResult(nodeRef))
            nodeRef.getProperty(PROP_NAME) shouldBe "changed"
        }
    }
}