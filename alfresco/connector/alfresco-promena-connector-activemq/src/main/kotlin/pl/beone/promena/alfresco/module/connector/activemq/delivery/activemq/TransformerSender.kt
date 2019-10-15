package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_RETRY_MAX_ATTEMPTS
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.PromenaJmsHeaders.SEND_BACK_TRANSFORMATION_PARAMETERS_STRING
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.TransformationParameters
import pl.beone.promena.alfresco.module.connector.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformer.TransformerId

class TransformerSender(
    private val transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
    private val authorizationService: AuthorizationService,
    private val transformationParametersSerializationService: TransformationParametersSerializationService,
    private val queueRequest: ActiveMQQueue,
    private val jmsTemplate: JmsTemplate
) {

    fun send(
        id: String,
        transformationDescriptor: TransformationDescriptor,
        nodeDescriptors: List<NodeDescriptor>,
        nodesChecksum: String,
        retry: Retry,
        attempt: Long
    ) {
        jmsTemplate.convertAndSend(queueRequest, transformationDescriptor) { message ->
            message.apply {
                jmsCorrelationID = id

                setStringProperty(TRANSFORMATION_HASH_CODE, transformationHashFunctionDeterminer.determine(getTransformationIds(transformationDescriptor)))

                val transformationParameters =
                    TransformationParameters(
                        nodeDescriptors,
                        nodesChecksum,
                        retry,
                        attempt,
                        authorizationService.getCurrentUser()
                    )
                setStringProperty(SEND_BACK_TRANSFORMATION_PARAMETERS, transformationParametersSerializationService.serialize(transformationParameters))
                setStringProperty(SEND_BACK_TRANSFORMATION_PARAMETERS_STRING, transformationParameters.toString())

                setLongProperty(SEND_BACK_ATTEMPT, attempt)
                setLongProperty(SEND_BACK_RETRY_MAX_ATTEMPTS, if (retry !is Retry.No) retry.maxAttempts else 0)
            }
        }
    }

    private fun getTransformationIds(transformationDescriptor: TransformationDescriptor): List<TransformerId> =
        transformationDescriptor.transformation.transformers
            .map(Transformation.Single::transformerId)
}