package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.client.messagebroker.startAsync
import pl.beone.promena.alfresco.module.client.messagebroker.startSync
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

class ActiveMQAlfrescoPromenaService(private val alfrescoNodesChecksumGenerator: AlfrescoNodesChecksumGenerator,
                                     private val alfrescoDataDescriptorGetter: AlfrescoDataDescriptorGetter,
                                     private val reactiveTransformationManager: ReactiveTransformationManager,
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

        logger.startSync(transformerId, nodeRefs, targetMediaType, determinedParameters, waitMax)

        return try {
            transform(transformerId, nodeRefs, targetMediaType, determinedParameters).get(waitMax)
        } catch (e: IllegalStateException) {
            throw TransformationSynchronizationException(transformerId, nodeRefs, targetMediaType, determinedParameters, waitMax)
        }
    }

    private fun <T> Mono<T>.get(waitMax: Duration?): T =
            if (waitMax != null) {
                block(waitMax)!!
            } else {
                block()!!
            }

    override fun transformAsync(transformerId: String,
                                nodeRefs: List<NodeRef>,
                                targetMediaType: MediaType,
                                parameters: Parameters?): Mono<List<NodeRef>> {
        val determinedParameters = determineParameters(parameters)

        logger.startAsync(transformerId, nodeRefs, targetMediaType, determinedParameters)

        return transform(transformerId, nodeRefs, targetMediaType, determinedParameters)
    }

    private fun transform(transformerId: String,
                          nodeRefs: List<NodeRef>,
                          targetMediaType: MediaType,
                          parameters: Parameters): Mono<List<NodeRef>> {
        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        val id = generateId()

        val transformation = reactiveTransformationManager.startTransformation(id)
        transformerSender.send(
                dataDescriptors, id, transformerId, nodeRefs, alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs), targetMediaType, parameters
        )
        return transformation
    }

    private fun determineParameters(parameters: Parameters?): Parameters =
            parameters ?: MapParameters.empty()

    private fun generateId(): String =
            UUID.randomUUID().toString()
}