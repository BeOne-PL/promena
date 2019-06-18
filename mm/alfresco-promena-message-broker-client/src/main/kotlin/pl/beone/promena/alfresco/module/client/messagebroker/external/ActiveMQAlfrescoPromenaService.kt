package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.service.cmr.repository.NodeRef
import org.slf4j.LoggerFactory
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.TransformationSynchronizationException
import pl.beone.promena.alfresco.module.client.base.common.startAsync
import pl.beone.promena.alfresco.module.client.base.common.startSync
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoPromenaService
import pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.TransformerSender
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager
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
            transform(generateId(), transformerId, nodeRefs, targetMediaType, determinedParameters, 0).get(waitMax)
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
                                parameters: Parameters?): Mono<List<NodeRef>> =
            transformAsync(generateId(), transformerId, nodeRefs, targetMediaType, parameters, 0)

    internal fun transformAsync(id: String,
                                transformerId: String,
                                nodeRefs: List<NodeRef>,
                                targetMediaType: MediaType,
                                parameters: Parameters?,
                                attempt: Long): Mono<List<NodeRef>> {
        val determinedParameters = determineParameters(parameters)

        logger.startAsync(transformerId, nodeRefs, targetMediaType, determinedParameters)

        return transform(id, transformerId, nodeRefs, targetMediaType, determinedParameters, attempt)
    }

    private fun transform(id: String,
                          transformerId: String,
                          nodeRefs: List<NodeRef>,
                          targetMediaType: MediaType,
                          parameters: Parameters,
                          attempt: Long): Mono<List<NodeRef>> {
        val dataDescriptors = alfrescoDataDescriptorGetter.get(nodeRefs)

        val nodesChecksum = alfrescoNodesChecksumGenerator.generateChecksum(nodeRefs)
        val transformation = reactiveTransformationManager.startTransformation(id)
        transformerSender.send(dataDescriptors, id, transformerId, nodeRefs, nodesChecksum, targetMediaType, parameters, attempt)
        return transformation
    }

    private fun determineParameters(parameters: Parameters?): Parameters =
            parameters ?: MapParameters.empty()

    private fun generateId(): String =
            UUID.randomUUID().toString()
}