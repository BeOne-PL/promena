package pl.beone.promena.alfresco.module.core.contract.node

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface TransformedDataDescriptorSaver {

    fun save(
        executionId: String,
        transformation: Transformation,
        nodeRefs: List<NodeRef>,
        transformedDataDescriptor: TransformedDataDescriptor
    ): List<NodeRef>
}