package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.internal.CompletedTransformationManager
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException

class ActiveMQAlfrescoPromenaService(private val completedTransformationManager: CompletedTransformationManager,
                                     private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                     private val transformerSender: TransformerSender)
    : AlfrescoPromenaService {

    companion object {
        private val logger = LoggerFactory.getLogger(ActiveMQAlfrescoPromenaService::class.java)
    }

    override fun transform(transformerId: String,
                           nodeRefs: List<NodeRef>,
                           targetMediaType: MediaType,
                           parameters: Parameters?,
                           waitMax: Duration?): List<NodeRef> {
        val determinedParameters = determineParameters(parameters)

        logger.info("Transforming <{}> <{}> nodes <{}> to <{}>. Waiting <{}> for response...",
                    transformerId,
                    determinedParameters.getAll(),
                    nodeRefs,
                    targetMediaType,
                    waitMax)

        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        val id = generateId()

        completedTransformationManager.startTransformation(id)
        transformerSender.send(dataDescriptors, id, transformerId, nodeRefs, targetMediaType, determinedParameters)

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
        val determinedParameters = determineParameters(parameters)

        logger.info("Transforming <{}> <{}> nodes <{}> to <{}>...",
                    transformerId,
                    determinedParameters.getAll(),
                    nodeRefs,
                    targetMediaType)

        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        transformerSender.send(dataDescriptors, generateId(), transformerId, nodeRefs, targetMediaType, determinedParameters)
    }

    private fun determineParameters(parameters: Parameters?): Parameters =
            parameters ?: MapParameters.empty()

    private fun generateId(): String =
            UUID.randomUUID().toString()
}