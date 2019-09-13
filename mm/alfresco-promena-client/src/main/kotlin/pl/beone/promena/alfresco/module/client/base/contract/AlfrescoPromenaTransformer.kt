package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import java.time.Duration

interface AlfrescoPromenaTransformer {

    fun transform(
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        renditionName: String? = null,
        waitMax: Duration? = null,
        retry: Retry? = null
    ): List<NodeRef>

    fun transformAsync(transformation: Transformation, nodeRefs: List<NodeRef>, renditionName: String? = null, retry: Retry? = null): Mono<List<NodeRef>>
}