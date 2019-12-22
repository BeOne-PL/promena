@file:Suppress("DEPRECATION")

package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.repo.thumbnail.ThumbnailDefinition
import org.alfresco.service.cmr.repository.TransformationOptions
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition

internal class PromenaThumbnailDefinition(
    internal val renditionDefinition: PromenaRenditionDefinition
) : ThumbnailDefinition() {

    override fun getName(): String =
        renditionDefinition.getRenditionName()

    override fun getMimetype(): String? =
        renditionDefinition.getTargetMediaType().mimeType

    override fun getTransformationOptions(): TransformationOptions? =
        null

    override fun getPlaceHolderResourcePath(): String? =
        renditionDefinition.getPlaceHolderResourcePath()

    override fun getMimeAwarePlaceHolderResourcePath(): String? =
        renditionDefinition.getMimeAwarePlaceHolderResourcePath()
}