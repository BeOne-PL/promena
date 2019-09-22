package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.repo.thumbnail.ThumbnailDefinition
import org.alfresco.service.cmr.repository.TransformationOptions
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

internal class PromenaThumbnailDefinition(
    private val renditionDefinition: AlfrescoPromenaRenditionDefinition
) : ThumbnailDefinition() {

    override fun getName(): String =
        renditionDefinition.getRenditionName()

    override fun getMimetype(): String? =
        null

    override fun getTransformationOptions(): TransformationOptions? =
        null

    override fun getPlaceHolderResourcePath(): String? =
        renditionDefinition.getPlaceHolderResourcePath()

    override fun getMimeAwarePlaceHolderResourcePath(): String? =
        renditionDefinition.getMimeAwarePlaceHolderResourcePath()
}