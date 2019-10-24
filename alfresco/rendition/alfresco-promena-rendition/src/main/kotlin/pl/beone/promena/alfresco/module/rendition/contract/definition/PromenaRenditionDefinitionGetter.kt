package pl.beone.promena.alfresco.module.rendition.contract.definition

interface PromenaRenditionDefinitionGetter {

    fun getAll(): List<PromenaRenditionDefinition>

    fun getByRenditionName(renditionName: String): PromenaRenditionDefinition
}