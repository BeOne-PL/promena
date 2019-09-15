package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.model.ContentModel
import org.alfresco.repo.thumbnail.ThumbnailRegistry
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.NodeService
import org.alfresco.service.cmr.repository.TransformationOptions
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo
import org.alfresco.service.cmr.thumbnail.ThumbnailParentAssociationDetails
import org.alfresco.service.cmr.thumbnail.ThumbnailService
import org.alfresco.service.namespace.QName
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionCoordinator
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionDefinitionManager

class PromenaThumbnailService(
    private val nodeService: NodeService,
    promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager,
    private val promenaRenditionCoordinator: PromenaRenditionCoordinator
) : ThumbnailService {

    private val thumbnailRegistry = PromenaThumbnailRegistry(promenaRenditionDefinitionManager)

    override fun getFailedThumbnails(sourceNode: NodeRef?): Map<String, FailedThumbnailInfo> =
        emptyMap()

    override fun getThumbnailRegistry(): ThumbnailRegistry =
        thumbnailRegistry

    override fun updateThumbnail(thumbnail: NodeRef, transformationOptions: TransformationOptions?) {
        promenaRenditionCoordinator.transform(thumbnail, nodeService.getProperty(thumbnail, ContentModel.PROP_NAME) as String)
    }

    override fun getThumbnails(node: NodeRef, contentProperty: QName, mimetype: String?, options: TransformationOptions?): List<NodeRef> {
        validateContentProperty(contentProperty)

        return promenaRenditionCoordinator.getRenditions(node).map { it.childRef }
    }

    override fun setThumbnailsEnabled(thumbnailsEnabled: Boolean) {

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

        return promenaRenditionCoordinator.transform(node, name)?.childRef
    }

    override fun getThumbnailByName(node: NodeRef, contentProperty: QName?, thumbnailName: String): NodeRef? {
        validateContentProperty(contentProperty)

        return promenaRenditionCoordinator.getRendition(node, thumbnailName)?.childRef
    }

    private fun validateContentProperty(contentProperty: QName?) {
        contentProperty?.let { require(contentProperty == ContentModel.PROP_CONTENT) { "Promena supports only <${ContentModel.PROP_CONTENT}> property" } }
    }
}