package pl.beone.promena.alfresco.module.rendition.internal.transformation

import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.model.PromenaTransformationModel.PROP_RENDITION_NAME
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.transformation.Transformation

class RenditionPromenaTransformationMetadataSaver(
    private val serviceRegistry: ServiceRegistry
) : PromenaTransformationMetadataSaver {

    override fun save(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptor: TransformedDataDescriptor,
        transformedDataNodeRefs: List<NodeRef>
    ) {
        try {
            val renditionName = getRenditionNameFromMetadata(transformedDataDescriptor.descriptors)
            transformedDataNodeRefs.forEach { serviceRegistry.nodeService.setProperty(it, PROP_RENDITION_NAME, renditionName) }
        } catch (e: NoSuchElementException) {
            // deliberately omitted. It isn't rendition transformation
        }
    }

    private fun getRenditionNameFromMetadata(transformedDataDescriptors: List<TransformedDataDescriptor.Single>): String =
        transformedDataDescriptors.mapNotNull { it.metadata.getOrNull(PROP_RENDITION_NAME.localName, String::class.java) }
            .distinct()
            .also { if(it.size > 1) error("Transformed data contain more than <1> rendition name: $it") }
            .firstOrNull() ?: throw NoSuchElementException()
}