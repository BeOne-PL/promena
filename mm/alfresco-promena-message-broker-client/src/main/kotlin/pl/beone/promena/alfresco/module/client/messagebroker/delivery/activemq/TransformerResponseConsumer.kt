package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.common.skippedSavingResult
import pl.beone.promena.alfresco.module.client.base.common.transformedSuccessfully
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoTransformedDataDescriptorSaver
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.MediaTypeConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.ParametersConverter
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor

class TransformerResponseConsumer(private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                  private val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                                  private val reactiveTransformationManager: ReactiveTransformationManager) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerResponseConsumer::class.java)
    }

    private val nodeRefsConverter = NodeRefsConverter()
    private val mediaTypeConverter = MediaTypeConverter()
    private val parametersConverter = ParametersConverter()

    @JmsListener(destination = "\${promena.client.message-broker.consumer.queue.response}")
    fun receiveQueue(@Header(CORRELATION_ID) correlationId: String,
                     @Header(PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Header(PROMENA_TRANSFORMATION_START_TIMESTAMP) startTimestamp: Long,
                     @Header(PROMENA_TRANSFORMATION_END_TIMESTAMP) endTimestamp: Long,
                     @Header(SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
                     @Header(SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE) rawMimeType: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET) rawCharset: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS) rawParameters: Map<String, Any>,
                     @Payload transformedDataDescriptors: List<TransformedDataDescriptor>) {
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)
        val targetMediaType = mediaTypeConverter.convert(rawMimeType, rawCharset)
        val parameters = parametersConverter.convert(rawParameters)

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            reactiveTransformationManager.completeErrorTransformation(
                    correlationId,
                    AnotherTransformationIsInProgressException(
                            transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, currentNodesChecksum
                    )
            )
            logger.skippedSavingResult(transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, currentNodesChecksum)
        } else {
            val targetNodeRefs = alfrescoTransformedDataDescriptorSaver.save(transformerId, nodeRefs, targetMediaType, transformedDataDescriptors)
            reactiveTransformationManager.completeTransformation(correlationId, targetNodeRefs)
            logger.transformedSuccessfully(transformerId, nodeRefs, targetMediaType, parameters, targetNodeRefs, startTimestamp, endTimestamp)
        }
    }

}