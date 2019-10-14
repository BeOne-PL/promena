package pl.beone.promena.alfresco.module.rendition.external.rendition2

import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2
import org.alfresco.repo.rendition2.RenditionService2
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.RenditionGetter

class PromenaRenditionService2(
    private val renditionGetter: RenditionGetter,
    private val promenaRenditionTransformer: PromenaRenditionTransformer,
    private val renditionDefinitionRegistry2: PromenaRenditionDefinitionRegistry2
) : RenditionService2 {

    override fun isEnabled(): Boolean =
        true

    override fun getRenditionByName(sourceNodeRef: NodeRef, renditionName: String): ChildAssociationRef? =
        renditionGetter.getRendition(sourceNodeRef, renditionName)

    override fun render(sourceNodeRef: NodeRef, renditionName: String) {
        promenaRenditionTransformer.transformAsync(sourceNodeRef, renditionName)
    }

    override fun getRenditions(sourceNodeRef: NodeRef): List<ChildAssociationRef> =
        renditionGetter.getRenditions(sourceNodeRef)

    override fun getRenditionDefinitionRegistry2(): RenditionDefinitionRegistry2 =
        renditionDefinitionRegistry2
}