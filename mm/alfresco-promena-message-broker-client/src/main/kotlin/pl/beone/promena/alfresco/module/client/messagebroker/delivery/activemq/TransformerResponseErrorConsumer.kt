package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.support.JmsHeaders.CORRELATION_ID
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.AnotherTransformationIsInProgressException
import pl.beone.promena.alfresco.module.client.base.common.couldNotTransform
import pl.beone.promena.alfresco.module.client.base.common.couldNotTransformButChecksumsAreDifferent
import pl.beone.promena.alfresco.module.client.base.common.logOnRetry
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.MediaTypeConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.NodeRefsConverter
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert.ParametersConverter
import pl.beone.promena.alfresco.module.client.messagebroker.external.ActiveMQAlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import reactor.core.publisher.Mono
import java.time.Duration

private data class ErrorTransformationDescriptor(val id: String,
                                                 val transformerId: String,
                                                 val nodeRefs: List<NodeRef>,
                                                 val targetMediaType: MediaType,
                                                 val parameters: Parameters,
                                                 val attempt: Long)

class TransformerResponseErrorConsumer(private val retryOnError: Boolean,
                                       private val retryOnErrorMaxAttempts: Long,
                                       private val retryOnErrorNextAttemptsDelay: Duration,
                                       private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                       private val reactiveTransformationManager: ReactiveTransformationManager,
                                       private val activeMQAlfrescoPromenaService: ActiveMQAlfrescoPromenaService) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerResponseErrorConsumer::class.java)
    }

    private val nodeRefsConverter = NodeRefsConverter()
    private val mediaTypeConverter = MediaTypeConverter()
    private val parametersConverter = ParametersConverter()

    @JmsListener(destination = "\${promena.client.message-broker.consumer.queue.response.error}",
                 selector = "\${promena.client.message-broker.consumer.queue.response.error.selector}")
    fun receiveQueue(@Header(CORRELATION_ID) correlationId: String,
                     @Header(PROMENA_TRANSFORMER_ID) transformerId: String,
                     @Header(SEND_BACK_NODE_REFS) rawNodeRefs: List<String>,
                     @Header(SEND_BACK_NODES_CHECKSUM) nodesChecksum: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE) rawMimeType: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET) rawCharset: String,
                     @Header(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS) rawParameters: Map<String, Any>,
                     @Header(SEND_BACK_ATTEMPT) attempt: Long,
                     @Payload exception: Exception) {
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
            logger.couldNotTransformButChecksumsAreDifferent(
                    transformerId, nodeRefs, targetMediaType, parameters, nodesChecksum, currentNodesChecksum, exception
            )
        } else {
            logger.couldNotTransform(transformerId, nodeRefs, targetMediaType, parameters, exception)

            if (makeAnotherAttempt(attempt)) {
                retry(correlationId, transformerId, nodeRefs, targetMediaType, parameters, attempt)

                if (lastAttempt(attempt)) {
                    reactiveTransformationManager.completeErrorTransformation(correlationId, exception)
                }
            }
        }
    }

    private fun makeAnotherAttempt(attempt: Long): Boolean =
            retryOnError && attempt < retryOnErrorMaxAttempts

    private fun lastAttempt(attempt: Long): Boolean =
            attempt == retryOnErrorMaxAttempts - 1

    private fun retry(id: String,
                      transformerId: String,
                      nodeRefs: List<NodeRef>,
                      targetMediaType: MediaType,
                      parameters: Parameters,
                      attempt: Long) {
        val currentAttempt = attempt + 1

        Mono.just(ErrorTransformationDescriptor(id, transformerId, nodeRefs, targetMediaType, parameters, currentAttempt))
                .doOnNext {
                    logger.logOnRetry(currentAttempt,
                                      retryOnErrorMaxAttempts,
                                      transformerId,
                                      parameters,
                                      nodeRefs,
                                      targetMediaType,
                                      retryOnErrorNextAttemptsDelay)
                }
                .delayElement(retryOnErrorNextAttemptsDelay)
                .doOnNext {
                    activeMQAlfrescoPromenaService.transformAsync(it.id, it.transformerId, it.nodeRefs, it.targetMediaType, it.parameters, it.attempt)
                }
                .subscribe()
    }
}