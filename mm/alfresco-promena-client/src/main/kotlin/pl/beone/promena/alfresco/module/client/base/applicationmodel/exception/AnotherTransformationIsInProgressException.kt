package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.model.Parameters

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class AnotherTransformationIsInProgressException(val transformerId: String,
                                                 val nodeRefs: List<NodeRef>,
                                                 val targetMediaType: MediaType,
                                                 val parameters: Parameters,
                                                 val oldNodesChecksum: String,
                                                 val currentNodesChecksum: String)
    : RuntimeException("Couldn't perform <$transformerId> transformation <$parameters> <$targetMediaType> on nodes <$nodeRefs>. Nodes were changed in the meantime (old checksum <$oldNodesChecksum>, current checksum <$currentNodesChecksum>). Another transformation is in progress...")