package pl.beone.promena.alfresco.module.client.base.external

import org.alfresco.model.ContentModel
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import pl.beone.promena.alfresco.module.client.base.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataConverter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.nio.charset.Charset

class ContentPropertyAlfrescoDataDescriptorGetter(
    private val nodeService: NodeService,
    private val contentService: ContentService,
    private val alfrescoDataConverter: AlfrescoDataConverter
) : AlfrescoDataDescriptorGetter {

    override fun get(nodeRefs: List<NodeRef>): DataDescriptor {
        nodeRefs.forEach { it.checkIfExists() }
        return dataDescriptor(nodeRefs.map(::convertToSingleDataDescriptor))
    }

    private fun NodeRef.checkIfExists() {
        if (!nodeService.exists(this)) {
            throw NodeDoesNotExist(this)
        }
    }

    private fun convertToSingleDataDescriptor(nodeRef: NodeRef): DataDescriptor.Single {
        val contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT)
        val mediaType = MediaType.of(contentReader.mimetype, Charset.forName(contentReader.encoding))

        return singleDataDescriptor(alfrescoDataConverter.createData(contentReader), mediaType, emptyMetadata())
    }
}