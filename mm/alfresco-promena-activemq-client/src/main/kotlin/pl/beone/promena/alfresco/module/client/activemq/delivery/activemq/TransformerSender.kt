package pl.beone.promena.alfresco.module.client.activemq.delivery.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.activemq.applicationmodel.PromenaAlfrescoJmsHeaders
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.noRetry
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import java.time.Duration
import javax.jms.Message

class TransformerSender(
    private val externalCommunication: ExternalCommunication,
    private val transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
    private val queueRequest: ActiveMQQueue,
    private val jmsTemplate: JmsTemplate
) {

    fun send(
        id: String,
        transformationDescriptor: TransformationDescriptor,
        nodeRefs: List<NodeRef>,
        nodesChecksum: String,
        retry: Retry,
        attempt: Long
    ) {
        jmsTemplate.convertAndSend(queueRequest, transformationDescriptor) { message ->
            message.apply {
                jmsCorrelationID = id

                val transformerIds = transformationDescriptor.transformation.transformers.map { it.transformerId }
                setStringProperty(PromenaJmsHeaders.TRANSFORMATION_HASH_CODE, transformationHashFunctionDeterminer.determine(transformerIds))

                setObjectProperty(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID, externalCommunication.id)
                externalCommunication.location?.let { setObjectProperty(PromenaAlfrescoJmsHeaders.COMMUNICATION_PARAMETERS_LOCATION, it.toString()) }

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_NODES_CHECKSUM, nodesChecksum)

                setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_ATTEMPT, attempt)
                if (retry == noRetry()) {
                    setRetryHeaders(false, 0, Duration.ZERO)
                } else {
                    setRetryHeaders(true, retry.maxAttempts, retry.nextAttemptDelay)
                }
            }
        }
    }

    private fun Message.setRetryHeaders(enabled: Boolean, maxAttempts: Long, nextAttemptDelay: Duration?) {
        setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_ENABLED, enabled)
        setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS, maxAttempts)
        setObjectProperty(PromenaAlfrescoJmsHeaders.SEND_BACK_RETRY_NEXT_ATTEMPT_DELAY, nextAttemptDelay.toString())
    }

}