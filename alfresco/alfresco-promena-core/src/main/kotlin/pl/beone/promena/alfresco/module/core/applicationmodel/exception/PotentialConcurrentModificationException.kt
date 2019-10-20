package pl.beone.promena.alfresco.module.core.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

data class PotentialConcurrentModificationException(
    val nodeRef: NodeRef
) : ConcurrentModificationException("Node <$nodeRef> has been modified in this transaction. It's highly probable that it may cause concurrency problems. Finish this transaction before a transformation")