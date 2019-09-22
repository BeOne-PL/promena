package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.repo.thumbnail.ThumbnailDefinition
import org.alfresco.service.cmr.repository.TransformationOptions

internal class PromenaThumbnailDefinition(
    private val renditionName: String
) : ThumbnailDefinition() {

    override fun getName(): String =
        renditionName

    override fun getMimetype(): String? =
        null

    override fun getTransformationOptions(): TransformationOptions? =
        null

    override fun getPlaceHolderResourcePath(): String =
        "alfresco/thumbnail/thumbnail_placeholder_$renditionName.png"

    override fun getMimeAwarePlaceHolderResourcePath(): String =
        "alfresco/thumbnail/thumbnail_placeholder_$renditionName{0}.png"
}