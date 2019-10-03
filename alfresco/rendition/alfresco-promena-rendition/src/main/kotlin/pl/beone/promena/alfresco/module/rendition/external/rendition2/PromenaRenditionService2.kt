package pl.beone.promena.alfresco.module.rendition.external.rendition2

import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2
import org.alfresco.repo.rendition2.RenditionService2
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionTransformer
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoRenditionGetter

class PromenaRenditionService2(
    private val alfrescoRenditionGetter: AlfrescoRenditionGetter,
    private val alfrescoPromenaRenditionTransformer: AlfrescoPromenaRenditionTransformer,
    private val renditionDefinitionRegistry2: PromenaRenditionDefinitionRegistry2
) : RenditionService2 {

    override fun isEnabled(): Boolean =
        true

    override fun getRenditionByName(sourceNodeRef: NodeRef, renditionName: String): ChildAssociationRef? =
        alfrescoRenditionGetter.getRendition(sourceNodeRef, renditionName)

    override fun render(sourceNodeRef: NodeRef, renditionName: String) {
        alfrescoPromenaRenditionTransformer.transformAsync(sourceNodeRef, renditionName)
    }

    override fun getRenditions(sourceNodeRef: NodeRef): List<ChildAssociationRef> =
        alfrescoRenditionGetter.getRenditions(sourceNodeRef)

    override fun getRenditionDefinitionRegistry2(): RenditionDefinitionRegistry2 =
        renditionDefinitionRegistry2
}