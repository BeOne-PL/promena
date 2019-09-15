package pl.beone.promena.alfresco.module.client.base.contract

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface AlfrescoTransformedDataDescriptorSaver {

    fun save(transformation: Transformation, nodeRefs: List<NodeRef>, transformedDataDescriptor: TransformedDataDescriptor): List<NodeRef>
}