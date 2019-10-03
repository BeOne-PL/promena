package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.model.ContentModel.PROP_CONTENT
import org.alfresco.model.ContentModel.PROP_NAME
import org.alfresco.repo.thumbnail.ThumbnailRegistry
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.cmr.repository.TransformationOptions
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails
import org.alfresco.service.cmr.thumbnail.ThumbnailService
import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter

class PromenaThumbnailService(
    private val nodeService: NodeService,
    private val thumbnailRegistry: ThumbnailRegistry,
    private val alfrescoRenditionGetter: AlfrescoRenditionGetter,
    private val alfrescoPromenaRenditionTransformer: AlfrescoPromenaRenditionTransformer
) : ThumbnailService {

    override fun getFailedThumbnails(sourceNode: NodeRef?): Map<String, FailedThumbnailInfo> =
        emptyMap()

    override fun getThumbnailRegistry(): ThumbnailRegistry =
        thumbnailRegistry

    override fun updateThumbnail(thumbnail: NodeRef, transformationOptions: TransformationOptions?) {
        alfrescoPromenaRenditionTransformer.transform(thumbnail, nodeService.getProperty(thumbnail, PROP_NAME) as String)
    }

    override fun getThumbnails(node: NodeRef, contentProperty: QName, mimetype: String?, options: TransformationOptions?): List<NodeRef> {
        validateContentProperty(contentProperty)

        return alfrescoRenditionGetter.getRenditions(node).map { it.childRef }
    }

    override fun setThumbnailsEnabled(thumbnailsEnabled: Boolean) {
        // deliberately omitted
    }

    override fun getThumbnailsEnabled(): Boolean =
        true

    override fun createThumbnail(
        node: NodeRef,
        contentProperty: QName?,
        mimetype: String?,
        transformationOptions: TransformationOptions?,
        name: String
    ): NodeRef? =
        createThumbnail(node, contentProperty, mimetype, transformationOptions, name, null)

    override fun createThumbnail(
        node: NodeRef,
        contentProperty: QName?,
        mimetype: String?,
        transformationOptions: TransformationOptions?,
        name: String,
        assocDetails: ThumbnailParentAssociationDetails?
    ): NodeRef? {
        validateContentProperty(contentProperty)

        return alfrescoPromenaRenditionTransformer.transform(node, name).childRef
    }

    override fun getThumbnailByName(node: NodeRef, contentProperty: QName?, thumbnailName: String): NodeRef? {
        validateContentProperty(contentProperty)

        return alfrescoRenditionGetter.getRendition(node, thumbnailName)?.childRef
    }

    private fun validateContentProperty(contentProperty: QName?) {
        contentProperty?.let { require(contentProperty == PROP_CONTENT) { "Promena supports only <$PROP_CONTENT> property" } }
    }
}