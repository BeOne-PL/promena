package pl.beone.promena.alfresco.module.connector.activemq.external.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import io.mockk.*
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.attempt
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.dataDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodeDescriptor
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodeRefs
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.nodesChecksum
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.retry
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformation
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.transformationExecution
import pl.beone.promena.alfresco.module.connector.activemq.TestConstants.userName
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PotentialConcurrentModificationException
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.noRetry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor

class ActiveMQPromenaTransformationExecutorTest {

    companion object {
        private val externalCommunicationParameters = memoryCommunicationParameters()

        private val transformationDescriptor = transformationDescriptor(
            transformation,
            dataDescriptor,
            externalCommunicationParameters
        )

        private val postTransformationExecutor = mockk<PostTransformationExecutor>()
        private val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            postTransformationExecutor,
            retry,
            dataDescriptor,
            nodesChecksum,
            attempt,
            userName
        )
    }

    private lateinit var promenaMutableTransformationManager: PromenaMutableTransformationManager
    private lateinit var postTransformationExecutorValidator: PostTransformationExecutorValidator
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
        postTransformationExecutorValidator = mockk {
            every { validate(any()) } just Runs
        }
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } just Runs
            every { verify(nodeRefs[1]) } just Runs
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
        ActiveMQPromenaTransformationExecutor(
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            noRetry(),
            postTransformationExecutorValidator,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformerSender,
            authorizationService
        ).execute(
            transformation,
            nodeDescriptor,
            postTransformationExecutor,
            retry
        ) shouldBe transformationExecution

        verify { transformerSender.send(transformationExecution.id, transformationDescriptor, transformationParameters) }
    }

    @Test
    fun execute_shouldUseDefaultRetry() {
        val defaultRetry = noRetry()
        ActiveMQPromenaTransformationExecutor(
            externalCommunicationParameters,
            promenaMutableTransformationManager,
            defaultRetry,
            postTransformationExecutorValidator,
            nodeInCurrentTransactionVerifier,
            nodesChecksumGenerator,
            dataDescriptorGetter,
            transformerSender,
            authorizationService
        ).execute(
            transformation,
            nodeDescriptor,
            postTransformationExecutor
        ) shouldBe transformationExecution

        verify { transformerSender.send(transformationExecution.id, transformationDescriptor, transformationParameters.copy(retry = defaultRetry)) }
    }

    @Test
    fun execute_oneOfNodesHasBeenChangedInCurrentTransaction_shouldThrowPotentialConcurrentModificationException() {
        val exception = PotentialConcurrentModificationException(nodeRefs[0])
        nodeInCurrentTransactionVerifier = mockk {
            every { verify(nodeRefs[0]) } throws exception
        }

        shouldThrowExactly<PotentialConcurrentModificationException> {
            ActiveMQPromenaTransformationExecutor(
                externalCommunicationParameters,
                promenaMutableTransformationManager,
                noRetry(),
                postTransformationExecutorValidator,
                nodeInCurrentTransactionVerifier,
                nodesChecksumGenerator,
                dataDescriptorGetter,
                transformerSender,
                authorizationService
            ).execute(
                transformation,
                nodeDescriptor,
                postTransformationExecutor
            )
        }.message shouldBe "Node <${nodeRefs[0]}> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Complete this transaction before executing the transformation"
    }

    @Test
    fun execute_postTransformationExecutorImplementationIsNotCorrect_shouldThrowIllegalArgumentException() {
        val illegalArgumentException = IllegalArgumentException("message")
        postTransformationExecutorValidator = mockk {
            every { validate(any()) } throws illegalArgumentException
        }

        shouldThrowExactly<IllegalArgumentException> {
            ActiveMQPromenaTransformationExecutor(
                externalCommunicationParameters,
                promenaMutableTransformationManager,
                noRetry(),
                postTransformationExecutorValidator,
                nodeInCurrentTransactionVerifier,
                nodesChecksumGenerator,
                dataDescriptorGetter,
                transformerSender,
                authorizationService
            ).execute(
                transformation,
                nodeDescriptor,
                postTransformationExecutor
            )
        }.message shouldBe "message"
    }
}