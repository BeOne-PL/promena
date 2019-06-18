package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
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
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

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
                     @Header(PROMENA_TRANSFORMATION_START_TIMESTAMP) transformationStartTimestamp: Long,
                     @Header(PROMENA_TRANSFORMATION_END_TIMESTAMP) transformationEndTimestamp: Long,
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
            handleDifferentChecksums(transformerId, nodeRefs, targetMediaType, parameters, correlationId, nodesChecksum, currentNodesChecksum)
        } else {
            handleSuccessTransformation(transformerId,
                                        nodeRefs,
                                        targetMediaType,
                                        parameters,
                                        transformedDataDescriptors,
                                        correlationId,
                                        transformationStartTimestamp,
                                        transformationEndTimestamp)
        }
    }

    private fun handleDifferentChecksums(transformerId: String,
                                         nodeRefs: List<NodeRef>,
                                         targetMediaType: MediaType,
                                         parameters: Parameters,
                                         correlationId: String,
                                         nodesChecksum: String,
                                         currentNodesChecksum: String) {
        reactiveTransformationManager.completeErrorTransformation(
                correlationId,
                AnotherTransformationIsInProgressException(transformerId,
                                                           nodeRefs,
                                                           targetMediaType,
                                                           parameters,
                                                           nodesChecksum,
                                                           currentNodesChecksum)
        )

        logger.warn("Skipped saving result <{}> transformation <{}> nodes <{}> to <{}> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress...",
                    transformerId,
                    parameters,
                    nodeRefs,
                    targetMediaType,
                    nodesChecksum,
                    currentNodesChecksum)
    }

    private fun handleSuccessTransformation(transformerId: String,
                                            nodeRefs: List<NodeRef>,
                                            targetMediaType: MediaType,
                                            parameters: Parameters,
                                            transformedDataDescriptors: List<TransformedDataDescriptor>,
                                            correlationId: String,
                                            transformationStartTimestamp: Long,
                                            transformationEndTimestamp: Long) {
        val targetNodeRefs =
                alfrescoTransformedDataDescriptorSaver.save(transformerId, nodeRefs, targetMediaType, transformedDataDescriptors)
        reactiveTransformationManager.completeTransformation(correlationId, targetNodeRefs)

        logger.info("Transformed <{}> <{}> nodes <{}> to <{}> <{}> in <{} s>",
                    transformerId,
                    parameters,
                    nodeRefs,
                    targetMediaType,
                    targetNodeRefs,
                    calculateExecutionTimeInSeconds(transformationStartTimestamp, transformationEndTimestamp))
    }

    private fun calculateExecutionTimeInSeconds(millisStart: Long, millisEnd: Long): String =
            String.format("%.3f", (millisEnd - millisStart) / 1000.0)
}