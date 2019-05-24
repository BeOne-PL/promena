package pl.beone.promena.alfresco.module.client.messagebroker.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor

interface AlfrescoDataDescriptorGetter {

    fun get(nodeRefs: List<NodeRef>): List<DataDescriptor>
}