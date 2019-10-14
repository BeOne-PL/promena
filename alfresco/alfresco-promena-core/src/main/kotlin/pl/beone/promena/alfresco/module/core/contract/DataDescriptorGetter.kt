package pl.beone.promena.alfresco.module.core.contract

import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.transformer.contract.data.DataDescriptor

interface DataDescriptorGetter {

    fun get(nodeDescriptors: List<NodeDescriptor>): DataDescriptor
}