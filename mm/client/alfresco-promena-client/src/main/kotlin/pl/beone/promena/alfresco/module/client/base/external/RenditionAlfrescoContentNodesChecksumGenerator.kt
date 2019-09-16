package pl.beone.promena.alfresco.module.client.base.external

import org.alfresco.model.ContentModel
import org.alfresco.repo.rendition2.RenditionService2Impl
import org.alfresco.service.cmr.repository.ContentData
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter
import pl.beone.promena.alfresco.module.client.base.contract.AlfrescoNodesChecksumGenerator

class RenditionAlfrescoContentNodesChecksumGenerator(
    private val nodeService: NodeService
) : AlfrescoNodesChecksumGenerator {

    override fun generateChecksum(nodeRefs: List<NodeRef>): String =
        if (nodeRefs.isEmpty()) {
            throw IllegalArgumentException("You must pass at least one node")
        } else {
            nodeRefs.joinToString("") { getSourceContentHashCode(it).toString() }
        }

    private fun getSourceContentHashCode(nodeRef: NodeRef): Int {
        val contentData = DefaultTypeConverter.INSTANCE.convert(
            ContentData::class.java,
            nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
        )

        return if (contentData != null) {
            // Originally we used the contentData URL, but that is not enough if the MimeType changes.
            (contentData.contentUrl + contentData.mimetype).hashCode()
        } else {
            RenditionService2Impl.SOURCE_HAS_NO_CONTENT
        }
    }
}