package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_COMMUNICATION_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_ATTEMPT
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_NODE_REFS
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

class TransformerSender(private val externalCommunication: ExternalCommunication,
                        private val queueRequest: ActiveMQQueue,
                        private val jmsTemplate: JmsTemplate) {

    fun send(dataDescriptors: List<DataDescriptor>,
             id: String,
             transformerId: String,
             nodeRefs: List<NodeRef>,
             nodesChecksum: String,
             targetMediaType: MediaType,
             parameters: Parameters,
             attempt: Long) {
        jmsTemplate.convertAndSend(queueRequest, TransformationDescriptor(dataDescriptors, targetMediaType, parameters)) { message ->
            message.apply {
                jmsCorrelationID = id
                setStringProperty(PROMENA_TRANSFORMER_ID, transformerId)

                setObjectProperty(PROMENA_COMMUNICATION_ID, externalCommunication.id)
                externalCommunication.location?.let { setObjectProperty(PROMENA_COMMUNICATION_LOCATION, externalCommunication.location.toString()) }

                setObjectProperty(SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(SEND_BACK_NODES_CHECKSUM, nodesChecksum)
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, targetMediaType.mimeType)
                setStringProperty(SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, targetMediaType.charset.name())
                setObjectProperty(SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, parameters.getAll())
                setObjectProperty(SEND_BACK_ATTEMPT, attempt)
            }
        }
    }
}