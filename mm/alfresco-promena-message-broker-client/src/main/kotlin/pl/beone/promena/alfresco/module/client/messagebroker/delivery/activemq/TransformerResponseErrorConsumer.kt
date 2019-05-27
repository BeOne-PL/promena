package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_END_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMATION_START_TIMESTAMP
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.MediaTypeConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.ParametersConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.TimestampConverter

class TransformerResponseErrorConsumer {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerResponseErrorConsumer::class.java)
    }

    private val timestampConverter = TimestampConverter()
    private val nodeRefsConverter = NodeRefsConverter()
    private val mediaTypeConverter = MediaTypeConverter()
    private val parametersConverter = ParametersConverter()

    @JmsListener(destination = "\${promena.client.message-broker.consumer.queue.response.error}")
    fun receiveQueue(@Headers headers: Map<String, Any>,
                     @Header(CORRELATION_ID) correlationId: String,
                     @Header(PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Header(PROMENA_TRANSFORMATION_START_TIMESTAMP) rawTransformationStartTimestamp: Long,
                     @Header(PROMENA_TRANSFORMATION_END_TIMESTAMP) rawTransformationEndTimestamp: Long,
                     @Header(SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE) rawMimeType: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET) rawCharset: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS) rawParameters: Map<String, Any>,
                     @Payload exception: Exception) {
        val transformationStartTimestamp = timestampConverter.convert(rawTransformationStartTimestamp)
        val transformationEndTimestamp = timestampConverter.convert(rawTransformationEndTimestamp)
        val nodeRefs = nodeRefsConverter.convert(rawNodeRefs)
        val mediaType = mediaTypeConverter.convert(rawMimeType, rawCharset)
        val parameters = parametersConverter.convert(rawParameters)

        logger.error("! {}, {}, {}, {}, {}, {}, {}",
                     correlationId,
                     transformerId,
                     transformationStartTimestamp,
                     transformationEndTimestamp,
                     nodeRefs,
                     mediaType,
                     parameters)
    }

}