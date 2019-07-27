package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import java.time.Duration

interface AlfrescoPromenaService {

    fun transform(
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        waitMax: Duration? = null,
        retry: Retry? = null
    ): List<NodeRef>

    fun transformAsync(
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        retry: Retry? = null
    ): Mono<List<NodeRef>>

}