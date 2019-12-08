package pl.beone.promena.alfresco.module.core.external.node

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.ContentReader
import org.alfresco.service.cmr.repository.InvalidNodeRefException
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.node.toNodeRefs
import pl.beone.promena.alfresco.module.core.contract.node.DataConverter
import pl.beone.promena.alfresco.module.core.contract.node.DataDescriptorGetter
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor

class ContentPropertyDataDescriptorGetter(
    private val dataConverter: DataConverter,
    private val serviceRegistry: ServiceRegistry
) : DataDescriptorGetter {

    override fun get(nodeDescriptor: NodeDescriptor): DataDescriptor {
        nodeDescriptor.toNodeRefs().forEach { it.checkIfExists() }
        return dataDescriptor(nodeDescriptor.descriptors.map(::convertToSingleDataDescriptor))
    }

    private fun NodeRef.checkIfExists() {
        if (!serviceRegistry.nodeService.exists(this)) {
            throw InvalidNodeRefException("Node <$this> doesn't exist", this)
        }
    }

    private fun convertToSingleDataDescriptor(nodeDescriptor: NodeDescriptor.Single): DataDescriptor.Single {
        val contentReader = serviceRegistry.contentService.getReader(nodeDescriptor.nodeRef, PROP_CONTENT)
            .also { checkIfNodeHasContent(nodeDescriptor.nodeRef, it) }
        val mediaType = mediaType(contentReader.mimetype, contentReader.encoding)

        return singleDataDescriptor(dataConverter.createData(contentReader), mediaType, nodeDescriptor.metadata)
    }

    private fun checkIfNodeHasContent(nodeRef: NodeRef, contentReader: ContentReader?) {
        if (contentReader == null) {
            error("Node <$nodeRef> has no content")
        }
    }
}