package pl.beone.promena.module.http.client.applicationmodel.descriptor

import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName

data class NodeDescriptor(val nodeRef: NodeRef,
                          val contentProperty: QName)