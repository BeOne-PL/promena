package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import mu.KotlinLogging
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.activemq.applicationmodel.PromenaAlfrescoJmsHeaders
import pl.beone.promena.alfresco.module.client.activemq.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoAuthenticationService
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.base.extension.skippedSavingResult
import pl.beone.promena.alfresco.module.client.base.extension.transformedSuccessfully
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor

class TransformerResponseConsumer(
    private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
    private val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
    private val alfrescoAuthenticationService: AlfrescoAuthenticationService,
    private val reactiveTransformationManager: ReactiveTransformationManager
) {

    companion object {
        private val logger = KotlinLogging.logger {}

        private val nodeRefsConverter = NodeRefsConverter()
    }

    @JmsListener(destination = "\${promena.client.activemq.consumer.queue.response}")
    fun receiveQueue(
        @Header(JmsHeaders.CORRELATION_ID) correlationId: String,
        @Header(PromenaJmsHeaders.TRANSFORMATION_START_TIMESTAMP) startTimestamp: Long,
        @Header(PromenaJmsHeaders.TRANSFORMATION_END_TIMESTAMP) endTimestamp: Long,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_RENDITION_NAME) renditionName: String?,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
        @Header(PromenaAlfrescoJmsHeaders.SEND_BACK_USER_NAME) userName: String,
        @Payload performedTransformationDescriptor: PerformedTransformationDescriptor
    ) {
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)

        val (transformation, transformedDataDescriptors) = performedTransformationDescriptor

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            reactiveTransformationManager.completeErrorTransformation(
                correlationId,
                AnotherTransformationIsInProgressException(transformation, nodeRefs, nodesChecksum, currentNodesChecksum)
            )

            logger.skippedSavingResult(transformation, nodeRefs, nodesChecksum, currentNodesChecksum)
        } else {
            val targetNodeRefs = alfrescoAuthenticationService.runAs(userName) {
                alfrescoTransformedDataDescriptorSaver.save(transformation, nodeRefs, transformedDataDescriptors, renditionName)
            }
            reactiveTransformationManager.completeTransformation(correlationId, targetNodeRefs)

            logger.transformedSuccessfully(transformation, nodeRefs, targetNodeRefs, startTimestamp, endTimestamp)
        }
    }

}