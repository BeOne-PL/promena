package pl.beone.promena.alfresco.module.client.base.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class NodeDoesNotExist(
    val nodeRef: NodeRef
) : RuntimeException("Node <$nodeRef> doesn't exist")