package pl.beone.promena.alfresco.module.client.base.contract

import pl.beone.promena.alfresco.module.client.base.applicationmodel.node.NodeDescriptor
import pl.beone.promena.transformer.contract.data.DataDescriptor

interface AlfrescoDataDescriptorGetter {

    fun get(nodeDescriptors: List<NodeDescriptor>): DataDescriptor
}