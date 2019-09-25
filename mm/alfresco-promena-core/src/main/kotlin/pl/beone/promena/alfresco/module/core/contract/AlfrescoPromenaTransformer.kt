package pl.beone.promena.alfresco.module.core.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.Retry
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import java.time.Duration

interface AlfrescoPromenaTransformer {

    fun transform(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, waitMax: Duration? = null, retry: Retry? = null): List<NodeRef>

    fun transformAsync(transformation: Transformation, nodeDescriptors: List<NodeDescriptor>, retry: Retry? = null): Mono<List<NodeRef>>
}