package pl.beone.promena.alfresco.module.rendition.external.rendition2

import org.alfresco.repo.rendition2.RenditionDefinition2
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionManager

class PromenaRenditionDefinitionRegistry2(
    private val promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager
) : RenditionDefinitionRegistry2 {

    override fun getRenditionNamesFrom(sourceMimetype: String?, size: Long): Set<String> =
        emptySet()

    override fun getRenditionNames(): Set<String> =
        promenaRenditionDefinitionManager.getAll()
            .map(PromenaAlfrescoRenditionDefinition::getRenditionName)
            .toSet()

    override fun getRenditionDefinition(renditionName: String): RenditionDefinition2 =
        PromenaRenditionDefinition2(renditionName)
}