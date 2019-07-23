package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.applicationmodel.retry.Retry
import pl.beone.promena.transformer.contract.transformation.Transformation
import reactor.core.publisher.Mono
import java.time.Duration

interface AlfrescoPromenaService {

    fun transform(nodeRefs: List<NodeRef>,
                  transformation: Transformation,
                  waitMax: Duration? = null,
                  retry: Retry? = null): List<NodeRef>

    fun transformAsync(nodeRefs: List<NodeRef>,
                       transformation: Transformation,
                       retry: Retry? = null): Mono<List<NodeRef>>

}