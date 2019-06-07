package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.MediaTypeConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.ParametersConverter
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import java.time.Duration

class TransformerResponseErrorConsumer(private val tryAgain: Boolean,
                                       private val tryAgainDelay: Duration,
                                       private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                       private val completedTransformationManager: CompletedTransformationManager,
                                       private val activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerResponseErrorConsumer::class.java)
    }

    private val nodeRefsConverter = NodeRefsConverter()
    private val mediaTypeConverter = MediaTypeConverter()
    private val parametersConverter = ParametersConverter()

    @JmsListener(destination = "\${promena.client.message-broker.consumer.queue.response.error}")
    fun receiveQueue(@Header(CORRELATION_ID) correlationId: String,
                     @Header(PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Header(SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
                     @Header(PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE) rawMimeType: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET) rawCharset: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS) rawParameters: Map<String, Any>,
                     @Payload exception: Exception) {
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)
        val mediaType = mediaTypeConverter.convert(rawMimeType, rawCharset)
        val parameters = parametersConverter.convert(rawParameters)

        val currentNodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        if (nodesChecksum != currentNodesChecksum) {
            completedTransformationManager.completeErrorTransformation(
                    correlationId,
                    AnotherTransformationIsInProgressException(transformerId, nodeRefs, mediaType, parameters, nodesChecksum, currentNodesChecksum)
            )

            logger.warn("Couldn't transform <{}> <{}> nodes <{}> to <{}> but nodes were changed in the meantime (old checksum <{}>, current checksum <{}>). Another transformation is in progress",
                        correlationId,
                        transformerId,
                        nodeRefs,
                        mediaType,
                        parameters,
                        exception)
        } else {
            completedTransformationManager.completeErrorTransformation(correlationId, exception)

            logger.error("Couldn't transform <{}> <{}> nodes <{}> to <{}>", correlationId, transformerId, nodeRefs, mediaType, parameters, exception)

            if (tryAgain) {
                GlobalScope.launch {
                    delay(tryAgainDelay.toMillis())

                    logger.info("Trying to transform <{}> <{}> nodes <{}> to <{}>...",
                                correlationId,
                                transformerId,
                                nodeRefs,
                                mediaType,
                                parameters,
                                exception)
                    activeMQAlfrescoPromenaService.transformAsync(transformerId, nodeRefs, mediaType, parameters)
                }
            }
        }
    }

}