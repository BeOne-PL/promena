package pl.beone.promena.alfresco.module.core.contract.transformation

import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

interface PromenaTransformationMetadataSaver {

    fun save(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptor: TransformedDataDescriptor,
        transformedNodeRefs: List<NodeRef>
    )
}