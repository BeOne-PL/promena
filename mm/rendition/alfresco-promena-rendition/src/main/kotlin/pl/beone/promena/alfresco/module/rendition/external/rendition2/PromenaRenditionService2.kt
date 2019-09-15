package pl.beone.promena.alfresco.module.rendition.external.rendition2

import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2
import org.alfresco.repo.rendition2.RenditionService2
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionCoordinator
import pl.beone.promena.alfresco.module.rendition.external.PromenaRenditionDefinitionManager

class PromenaRenditionService2(
    promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager,
    private val promenaRenditionCoordinator: PromenaRenditionCoordinator
) : RenditionService2 {

    private val renditionDefinitionRegistry2 = PromenaRenditionDefinitionRegistry2(promenaRenditionDefinitionManager)

    override fun isEnabled(): Boolean =
        true

    override fun getRenditionByName(sourceNodeRef: NodeRef, renditionName: String): ChildAssociationRef? =
        promenaRenditionCoordinator.getRendition(sourceNodeRef, renditionName)

    override fun render(sourceNodeRef: NodeRef, renditionName: String) {
        promenaRenditionCoordinator.transformAsync(sourceNodeRef, renditionName)
    }

    override fun getRenditions(sourceNodeRef: NodeRef): List<ChildAssociationRef> =
        promenaRenditionCoordinator.getRenditions(sourceNodeRef)

    override fun getRenditionDefinitionRegistry2(): RenditionDefinitionRegistry2 =
        renditionDefinitionRegistry2
}