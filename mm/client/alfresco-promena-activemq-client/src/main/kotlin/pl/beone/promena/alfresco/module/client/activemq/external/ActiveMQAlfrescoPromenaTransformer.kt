package pl.beone.promena.alfresco.module.client.activemq.external

import mu.KotlinLogging
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaTransformer
import pl.beone.promena.alfresco.module.client.base.extension.startAsync
import pl.beone.promena.alfresco.module.client.base.extension.startSync
import pl.beone.promena.core.applicationmodel.transformation.transformationDescriptor
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

class ActiveMQAlfrescoPromenaTransformer(
    private val externalCommunicationParameters: CommunicationParameters,
    private val retry: Retry,
    private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
    private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
    private val reactiveTransformationManager: ReactiveTransformationManager,
    private val transformerSender: TransformerSender
) : AlfrescoPromenaTransformer {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun transform(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, waitMax: Duration?, retry: Retry?): List<NodeRef> {
        logger.startSync(transformation, nodeDescriptors, waitMax)

        return try {
            transform(generateId(), transformation, nodeDescriptors, determineRetry(retry), 0).get(waitMax)
        } catch (e: IllegalStateException) {
            throw TransformationSynchronizationException(transformation, nodeDescriptors, waitMax)
        }
    }

    private fun <T> Mono<T>.get(waitMax: Duration?): T =
        if (waitMax != null) {
            block(waitMax)!!
        } else {
            block()!!
        }

    override fun transformAsync(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, retry: Retry?): Mono<List<NodeRef>> =
        transformAsync(generateId(), transformation, nodeDescriptors, determineRetry(retry), 0)

    private fun determineRetry(retry: Retry?): Retry =
        retry ?: this.retry

    internal fun transformAsync(
        id: String,
        transformation: Transformation,
        nodeDescriptors: List<NodeDescriptor>,
        retry: Retry,
        attempt: Long
    ): Mono<List<NodeRef>> {
        logger.startAsync(transformation, nodeDescriptors)

        return transform(id, transformation, nodeDescriptors, retry, attempt)
    }

    private fun transform(
        id: String,
        transformation: Transformation,
        nodeDescriptors: List<NodeDescriptor>,
        retry: Retry,
        attempt: Long
    ): Mono<List<NodeRef>> {
        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeDescriptors)

        val nodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeDescriptors.toNodeRefs())
        val reactiveTransformation = reactiveTransformationManager.startTransformation(id)
        transformerSender.send(
            id,
            transformationDescriptor(transformation, dataDescriptors, externalCommunicationParameters),
            nodeDescriptors,
            nodesChecksum,
            retry,
            attempt
        )
        return reactiveTransformation
    }

    private fun generateId(): String =
        UUID.randomUUID().toString()
}