package pl.beone.promena.alfresco.module.core.applicationmodel.exception

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.extension.toPrettyString
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

class TransformationSynchronizationException(
    val transformation: Transformation,
    val nodeDescriptors: List<NodeDescriptor>,
    val waitMax: Duration?
) : RuntimeException("Synchronization time <${waitMax.toPrettyString()}> for <$transformation> on <$nodeDescriptors> expired")