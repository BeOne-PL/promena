package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class TransformationSynchronizationException(val transformerId: String,
                                             val nodeRefs: List<NodeRef>,
                                             val targetMediaType: MediaType,
                                             val parameters: Parameters,
                                             val waitMax: Duration?)
    : RuntimeException("Synchronization time <$waitMax> for transformation <$transformerId> <$parameters> <$targetMediaType> on nodes <$nodeRefs> expired")