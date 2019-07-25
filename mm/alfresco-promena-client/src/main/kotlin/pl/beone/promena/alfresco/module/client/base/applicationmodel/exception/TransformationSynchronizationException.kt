package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class TransformationSynchronizationException(val transformation: Transformation,
                                             val nodeRefs: List<NodeRef>,
                                             val waitMax: Duration?)
    : RuntimeException("Synchronization time <$waitMax> for <$transformation> on nodes <$nodeRefs> expired")