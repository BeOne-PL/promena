package pl.beone.promena.alfresco.module.connector.activemq.external.transformation

import mu.KotlinLogging
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.alfresco.module.core.contract.node.NodeInCurrentTransactionVerifier
import pl.beone.promena.alfresco.module.core.contract.node.NodesChecksumGenerator
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationManager.PromenaMutableTransformationManager
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.alfresco.module.core.extension.start
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class ActiveMQPromenaTransformationExecutor(
    private val externalCommunicationParameters: CommunicationParameters,
    private val promenaMutableTransformationManager: PromenaMutableTransformationManager,
    private val retry: Retry,
    private val postTransformationExecutorValidator: PostTransformationExecutorValidator,
    private val nodeInCurrentTransactionVerifier: NodeInCurrentTransactionVerifier,
    private val nodesChecksumGenerator: NodesChecksumGenerator,
    private val dataDescriptorGetter: DataDescriptorGetter,
    private val transformerSender: TransformerSender,
    private val authorizationService: AuthorizationService
) : PromenaTransformationExecutor {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun execute(
        transformation: Transformation,
        nodeDescriptor: NodeDescriptor,
        postTransformationExecutor: PostTransformationExecutor?,
        retry: Retry?
    ): TransformationExecution {
        val nodeRefs = nodeDescriptor.toNodeRefs()

        postTransformationExecutor?.let(postTransformationExecutorValidator::validate)
        nodeRefs.forEach(nodeInCurrentTransactionVerifier::verify)

        val nodesChecksum = nodesChecksumGenerator.generate(nodeRefs)
        val userName = authorizationService.getCurrentUser()

        val dataDescriptor = dataDescriptorGetter.get(nodeDescriptor)

        val transformationExecution = promenaMutableTransformationManager.startTransformation()

        logger.start(transformation, nodeDescriptor)

        val transformationParameters = TransformationParameters(
            transformation,
            nodeDescriptor,
            postTransformationExecutor,
            determineRetry(retry),
            dataDescriptor,
            nodesChecksum,
            0,
            userName
        )

        execute(transformationExecution.id, transformation, dataDescriptor, transformationParameters)

        return transformationExecution
    }

    internal fun execute(
        id: String,
        transformation: Transformation,
        dataDescriptor: DataDescriptor,
        transformationParameters: TransformationParameters
    ) {
        transformerSender.send(
            id,
            transformationDescriptor(transformation, dataDescriptor, externalCommunicationParameters),
            transformationParameters
        )
    }

    private fun determineRetry(retry: Retry?): Retry =
        retry ?: this.retry
}