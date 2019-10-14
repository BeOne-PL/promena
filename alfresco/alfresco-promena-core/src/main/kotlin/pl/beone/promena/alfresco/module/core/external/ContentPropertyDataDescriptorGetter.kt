package pl.beone.promena.alfresco.module.core.external

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.DataConverter
import pl.beone.promena.alfresco.module.core.contract.DataDescriptorGetter
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor

class ContentPropertyDataDescriptorGetter(
    private val nodeService: NodeService,
    private val contentService: ContentService,
    private val dataConverter: DataConverter
) : DataDescriptorGetter {

    override fun get(nodeDescriptors: List<NodeDescriptor>): DataDescriptor {
        nodeDescriptors.map(NodeDescriptor::nodeRef).forEach { it.checkIfExists() }
        return dataDescriptor(nodeDescriptors.map(::convertToSingleDataDescriptor))
    }

    private fun NodeRef.checkIfExists() {
        if (!nodeService.exists(this)) {
            throw NodeDoesNotExist(this)
        }
    }

    private fun convertToSingleDataDescriptor(nodeDescriptor: NodeDescriptor): DataDescriptor.Single {
        val contentReader = contentService.getReader(nodeDescriptor.nodeRef, PROP_CONTENT)
        val mediaType = mediaType(contentReader.mimetype, contentReader.encoding)

        return singleDataDescriptor(dataConverter.createData(contentReader), mediaType, nodeDescriptor.metadata)
    }
}