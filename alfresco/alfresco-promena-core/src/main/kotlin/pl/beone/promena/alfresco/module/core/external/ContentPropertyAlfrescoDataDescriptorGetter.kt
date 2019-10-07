package pl.beone.promena.alfresco.module.core.external

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.core.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import java.nio.charset.Charset

class ContentPropertyAlfrescoDataDescriptorGetter(
    private val nodeService: NodeService,
    private val contentService: ContentService,
    private val alfrescoDataConverter: AlfrescoDataConverter
) : AlfrescoDataDescriptorGetter {

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

        return singleDataDescriptor(alfrescoDataConverter.createData(contentReader), mediaType, nodeDescriptor.metadata)
    }
}