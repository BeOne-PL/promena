package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.client.base.extension.toPrettyString
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

class TransformationSynchronizationException(
    val transformation: Transformation,
    val nodeRefs: List<NodeRef>,
    val waitMax: Duration?
) : RuntimeException("Synchronization time <${waitMax.toPrettyString()}> for <$transformation> on nodes <$nodeRefs> expired")