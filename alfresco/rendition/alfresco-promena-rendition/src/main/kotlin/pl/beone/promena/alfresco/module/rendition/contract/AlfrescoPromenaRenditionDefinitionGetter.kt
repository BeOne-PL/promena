package pl.beone.promena.alfresco.module.rendition.contract

interface AlfrescoPromenaRenditionDefinitionGetter {

    fun getAll(): List<AlfrescoPromenaRenditionDefinition>

    fun getByRenditionName(renditionName: String): AlfrescoPromenaRenditionDefinition
}