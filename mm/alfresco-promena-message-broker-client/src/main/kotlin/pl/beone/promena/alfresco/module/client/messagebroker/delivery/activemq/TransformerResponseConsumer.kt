package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

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
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.TimestampConverter
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TransformerResponseConsumer(private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                  private val alfrescoTransformedDataDescriptorSaver: AlfrescoTransformedDataDescriptorSaver,
                                  private val completedTransformationManager: CompletedTransformationManager) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerResponseConsumer::class.java)
    }

    private val timestampConverter = TimestampConverter()
    private val nodeRefsConverter = NodeRefsConverter()
    private val mediaTypeConverter = MediaTypeConverter()
    private val parametersConverter = ParametersConverter()

    @JmsListener(destination = "\${promena.client.message-broker.consumer.queue.response}")
    fun receiveQueue(@Header(CORRELATION_ID) correlationId: String,
                     @Header(PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Header(PROMENA_TRANSFORMATION_START_TIMESTAMP) rawTransformationStartTimestamp: Long,
                     @Header(PROMENA_TRANSFORMATION_END_TIMESTAMP) rawTransformationEndTimestamp: Long,
                     @Header(SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
                     @Header(SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE) rawMimeType: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET) rawCharset: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS) rawParameters: Map<String, Any>,
                     @Payload transformedDataDescriptors: List<TransformedDataDescriptor>) {
        val transformationStartTimestamp = timestampConverter.convert(rawTransformationStartTimestamp)
        val transformationEndTimestamp = timestampConverter.convert(rawTransformationEndTimestamp)
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)
        val mediaType = mediaTypeConverter.convert(rawMimeType, rawCharset)
        val parameters = parametersConverter.convert(rawParameters)

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            completedTransformationManager.completeErrorTransformation(
                    correlationId,
                    AnotherTransformationIsInProgressException(transformerId, nodeRefs, mediaType, parameters, nodesChecksum, currentNodesChecksum)
            )

            logger.warn("Skipped saving result of <{}> transformation <{}> nodes <{}> to <{}> in <{} s> because nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress",
                        transformerId,
                        parameters.getAll(),
                        nodeRefs,
                        mediaType,
                        calculateExecutionTimeInSeconds(transformationStartTimestamp, transformationEndTimestamp),
                        nodesChecksum,
                        currentNodesChecksum)
        } else {
            val transformedNodeRefs =
                    alfrescoTransformedDataDescriptorSaver.save(transformerId, nodeRefs, mediaType, transformedDataDescriptors)
            completedTransformationManager.completeTransformation(correlationId, transformedNodeRefs)

            logger.info("Transformed <{}> <{}> nodes <{}> to <{}> in <{} s>",
                        transformerId,
                        parameters.getAll(),
                        nodeRefs,
                        mediaType,
                        calculateExecutionTimeInSeconds(transformationStartTimestamp, transformationEndTimestamp))
        }
    }

    private fun calculateExecutionTimeInSeconds(first: LocalDateTime, second: LocalDateTime): Long =
            first.until(second, ChronoUnit.SECONDS)

}