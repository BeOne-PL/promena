package pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel.exception

import org.alfresco.service.cmr.repository.NodeRef

class NodeDoesNotExist(val nodeRef: NodeRef) : RuntimeException("Node <$nodeRef> doesn't exist")