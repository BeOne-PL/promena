package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import org.alfresco.repo.thumbnail.ThumbnailDefinition
import org.alfresco.repo.thumbnail.ThumbnailRegistry
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionDefinitionManager

internal class PromenaThumbnailRegistry(
    private val promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager
) : ThumbnailRegistry() {

    init {
        promenaRenditionDefinitionManager.getAll().forEach {
            addThumbnailDefinition(PromenaThumbnailDefinition(it.getRenditionName()))
        }
    }

    override fun isThumbnailDefinitionAvailable(
        sourceUrl: String?,
        sourceMimetype: String?,
        sourceSize: Long,
        sourceNodeRef: NodeRef?,
        thumbnailDefinition: ThumbnailDefinition
    ): Boolean =
        try {
            promenaRenditionDefinitionManager.getByRenditionName(thumbnailDefinition.name)
            true
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            false
        }
}