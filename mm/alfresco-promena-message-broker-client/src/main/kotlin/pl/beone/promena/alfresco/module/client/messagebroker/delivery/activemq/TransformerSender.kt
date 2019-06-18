package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq

import org.alfresco.service.cmr.repository.NodeRef
import org.apache.activemq.command.ActiveMQQueue
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.net.URI

class TransformerSender(private val communicationLocation: URI?,
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
                setStringProperty(PromenaJmsHeader.PROMENA_TRANSFORMER_ID, transformerId)

                communicationLocation?.let { setObjectProperty(PromenaJmsHeader.PROMENA_COMMUNICATION_LOCATION, communicationLocation) }

                setObjectProperty(PromenaJmsHeader.SEND_BACK_NODE_REFS, nodeRefs.map { it.toString() })
                setObjectProperty(PromenaJmsHeader.SEND_BACK_NODES_CHECKSUM, nodesChecksum)
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_MIME_TYPE, targetMediaType.mimeType)
                setStringProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_CHARSET, targetMediaType.charset.name())
                setObjectProperty(PromenaJmsHeader.SEND_BACK_TARGET_MEDIA_TYPE_PARAMETERS, parameters.getAll())
                setObjectProperty(PromenaJmsHeader.SEND_BACK_ATTEMPT, attempt)
            }
        }
    }
}