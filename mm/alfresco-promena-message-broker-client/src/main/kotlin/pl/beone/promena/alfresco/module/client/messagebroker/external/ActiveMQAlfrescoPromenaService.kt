package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.net.URI
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException

class ActiveMQAlfrescoPromenaService(private val communicationLocation: URI?,
                                     private val completedTransformationManager: CompletedTransformationManager,
                                     private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                     private val queueRequest: ActiveMQQueue,
                                     private val jmsTemplate: JmsTemplate)
    : AlfrescoPromenaService {

    companion object {
        private val logger = LoggerFactory.getLogger(ActiveMQAlfrescoPromenaService::class.java)
    }

    override fun transform(transformerId: String,
                           nodeRefs: List<NodeRef>,
                           targetMediaType: MediaType,
                           parameters: Parameters?,
                           waitMax: Duration?): List<NodeRef> {
        val determinedParameters = parameters ?: MapParameters.empty()

        logger.info("Transforming <{}> <{}> nodes <{}> to <{}>. Waiting <{}> for response...",
                    transformerId,
                    determinedParameters.getAll(),
                    nodeRefs,
                    targetMediaType,
                    waitMax)

        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        val id = generateId()

        sendMessageToActiveMQ(dataDescriptors, id, transformerId, nodeRefs, targetMediaType, determinedParameters)

        completedTransformationManager.startTransformation(id)
        return try {
            completedTransformationManager.getTransformedNodeRefs(id, waitMax)
        } catch (e: TimeoutException) {
            throw TransformationSynchronizationException(transformerId, nodeRefs, targetMediaType, determinedParameters, waitMax)
        }
    }

    override fun transformAsync(transformerId: String,
                                nodeRefs: List<NodeRef>,
                                targetMediaType: MediaType,
                                parameters: Parameters?) {
        val determinedParameters = parameters ?: MapParameters.empty()

        logger.info("Transforming <{}> <{}> nodes <{}> to <{}>...",
                    transformerId,
                    determinedParameters.getAll(),
                    nodeRefs,
                    targetMediaType)

        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        sendMessageToActiveMQ(dataDescriptors, generateId(), transformerId, nodeRefs, targetMediaType, determinedParameters)
    }

    private fun sendMessageToActiveMQ(dataDescriptors: List<DataDescriptor>,
                                      id: String,
                                      transformerId: String,
                                      nodeRefs: List<NodeRef>,
                                      targetMediaType: MediaType,
                                      parameters: Parameters) {
        jmsTemplate.convertAndSend(queueRequest, dataDescriptors) { message ->
            message.apply {
                jmsCorrelationID = id
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

                communicationLocation?.let { setObjectProperty(PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION, communicationLocation) }

                setObjectProperty(PromenaJmsHeader.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, targetMediaType.mimeType)
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, targetMediaType.charset.name())
                setObjectProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, parameters.getAll())
            }
        }
    }

    private fun generateId(): String =
            UUID.randomUUID().toString()
}