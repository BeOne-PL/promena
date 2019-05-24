package pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel

import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName

data class NodeDescriptor(val nodeRef: NodeRef,
                          val contentProperty: QName)