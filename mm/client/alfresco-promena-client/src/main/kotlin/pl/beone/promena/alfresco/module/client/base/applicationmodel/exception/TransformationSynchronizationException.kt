package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.client.base.extension.toPrettyString
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

class TransformationSynchronizationException(
    val transformation: Transformation,
    val nodeDescriptors: List<NodeDescriptor>,
    val waitMax: Duration?
) : RuntimeException("Synchronization time <${waitMax.toPrettyString()}> for <$transformation> on <$nodeDescriptors> expired")