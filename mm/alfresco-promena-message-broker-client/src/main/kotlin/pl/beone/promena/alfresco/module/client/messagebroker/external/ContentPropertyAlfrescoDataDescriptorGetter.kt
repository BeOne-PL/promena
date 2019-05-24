package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.model.ContentModel
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import pl.beone.promena.alfresco.module.client.messagebroker.applicationmodel.exception.NodeDoesNotExist
import pl.beone.promena.alfresco.module.client.messagebroker.contract.AlfrescoDataDescriptorGetter
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import java.nio.charset.Charset

class ContentPropertyAlfrescoDataDescriptorGetter(private val nodeService: NodeService,
                                                  private val contentService: ContentService,
                                                  private val alfrescoDataConverter: FileAlfrescoDataConverter) : AlfrescoDataDescriptorGetter {

    override fun get(nodeRefs: List<NodeRef>): List<DataDescriptor> {
        nodeRefs.forEach { it.checkIfExists() }
        return nodeRefs.map { it.convertToDataDescriptor() }
    }

    private fun NodeRef.checkIfExists() {
        if (!nodeService.exists(this)) {
            throw NodeDoesNotExist(this)
        }
    }

    private fun NodeRef.convertToDataDescriptor(): DataDescriptor {
        val contentReader = contentService.getReader(this, ContentModel.PROP_CONTENT)
        val mediaType = MediaType.create(contentReader.mimetype, Charset.forName(contentReader.encoding))

        return DataDescriptor(alfrescoDataConverter.createData(contentReader), mediaType)
    }
}