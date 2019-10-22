package pl.beone.promena.alfresco.module.connector.activemq.external.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.*
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toSingleNodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.PostTransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.transformationExecution
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.data.toMemoryData
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import pl.beone.promena.transformer.internal.model.metadata.plus
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.time.Duration

class ActiveMQPromenaTransformationExecutorTest {

    companion object {
        private val externalCommunicationParameters = memoryCommunicationParameters()

        private val transformationExecution = transformationExecution("1")

        private val transformation = singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value"))
        private val dataDescriptor = singleDataDescriptor("test".toMemoryData(), TEXT_PLAIN, emptyMetadata() + ("key" to "value"))
        private val transformationDescriptor = transformationDescriptor(
            transformation,
            dataDescriptor,
            externalCommunicationParameters
        )

        private val nodeDescriptor =
            NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c").toSingleNodeDescriptor(emptyMetadata() + ("key" to "value"))
        private val nodeRefs = nodeDescriptor.toNodeRefs()
        private val postTransformationExecution = mockk<PostTransformationExecution>()
        private val retry = customRetry(3, Duration.ofMillis(1000))
        private const val nodesChecksum = "123456789"
        private const val userName = "admin"
        private val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            postTransformationExecution,
            retry,
            dataDescriptor,
            nodesChecksum,
            0,
            userName
        )
    }

    private lateinit var promenaMutableTransformationManager: PromenaMutableTransformationManager
    private lateinit var nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier
    private lateinit var nodesChecksumGenerator: NodesChecksumGenerator
    private lateinit var dataDescriptorGetter: DataDescriptorGetter
    private lateinit var transformerSender: TransformerSender
    private lateinit var authorizationService: AuthorizationService

    @Before
    fun setUp() {
        promenaMutableTransformationManager = mockk {
            every { startTransformation() } returns transformationExecution
        }
        nodesChecksumGenerator = mockk {
            every { generate(nodeRefs) } returns nodesChecksum
        }
        dataDescriptorGetter = mockk {
            every { get(nodeDescriptor) } returns dataDescriptor
        }
        transformerSender = mockk {
            every { send(any(), transformationDescriptor, any()) } just Runs
        }
        authorizationService = mockk {
            every { getCurrentUser() } returns userName
        }
    }

    @Test
    fun execute() {
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } just Runs
        }

        ActiveMQPromenaTransformationExecutor(
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            noRetry(),
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformerSender,
            authorizationService
        ).execute(
            transformation,
            nodeDescriptor,
            postTransformationExecution,
            retry
        ) shouldBe transformationExecution

        verify { transformerSender.send(transformationExecution.id, transformationDescriptor, transformationParameters) }
    }

    @Test
    fun execute_shouldUseDefaultRetry() {
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } just Runs
        }

        val defaultRetry = noRetry()
        ActiveMQPromenaTransformationExecutor(
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            defaultRetry,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformerSender,
            authorizationService
        ).execute(
            transformation,
            nodeDescriptor,
            postTransformationExecution
        ) shouldBe transformationExecution

        verify { transformerSender.send(transformationExecution.id, transformationDescriptor, transformationParameters.copy(retry = defaultRetry)) }
    }

    @Test
    fun execute_oneOfNodesHasBeenChangedInCurrentTransaction_shouldThrowPotentialConcurrentModificationException() {
        val potentialConcurrentModificationException = PotentialConcurrentModificationException(nodeRefs[0])
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } throws potentialConcurrentModificationException
        }

        shouldThrowExactly<PotentialConcurrentModificationException> {
            ActiveMQPromenaTransformationExecutor(
                externalCommunicationParameters,
                promenaMutableTransformationManager,
                noRetry(),
                nodeInCurrentTransactionVerifier,
                nodesChecksumGenerator,
                dataDescriptorGetter,
                transformerSender,
                authorizationService
            ).execute(
                transformation,
                nodeDescriptor,
                postTransformationExecution
            )
        }.message shouldBe "Node <${nodeRefs[0]}> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Finish this transaction before a transformation"
    }
}