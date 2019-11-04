package pl.beone.promena.alfresco.module.connector.activemq.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import org.alfresco.model.ContentModel.PROP_NAME
import org.alfresco.rad.test.AlfrescoTestRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.connector.activemq.AbstractUtilsAlfrescoIT
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.attempt
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.dataDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodeDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodesChecksum
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.retry
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.userName
import pl.beone.promena.alfresco.module.connector.activemq.external.transformation.TransformationParameters
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorInjector
import pl.beone.promena.alfresco.module.core.external.transformation.post.ReflectionPostTransformationExecutorInjector
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.transformation.Transformation

@RunWith(AlfrescoTestRunner::class)
class TransformationParametersSerializationServiceTest : AbstractUtilsAlfrescoIT() {

    companion object {
        private val serializationService = TransformationParametersSerializationService(KryoSerializationService())
    }

    private lateinit var postTransformationExecutorInjector: PostTransformationExecutorInjector

    @Before
    fun setUp() {
        postTransformationExecutorInjector = ReflectionPostTransformationExecutorInjector(applicationContext, serviceRegistry)
    }

    @Test
    fun serializeAndDeserialize() {
        val nodeRef = createOrGetIntegrationTestsFolder().createNode()
        val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            TestPostTransformationExecutor,
            retry,
            dataDescriptor,
            nodesChecksum,
            attempt,
            userName
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

            it.postTransformationExecutor shouldNotBe null
            postTransformationExecutorInjector.inject(it.postTransformationExecutor!!)
            it.postTransformationExecutor!!.execute(mockk(), mockk(), transformationExecutionResult(nodeRef))
            nodeRef.getProperty(PROP_NAME) shouldBe "changed"
        }
    }

    private object TestPostTransformationExecutor : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
            serviceRegistry.nodeService.setProperty(result.nodeRefs[0], PROP_NAME, "changed")
        }
    }
}