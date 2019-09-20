package pl.beone.promena.alfresco.module.rendition.contract

interface PromenaRenditionDefinitionManager {

    fun getAll(): List<PromenaAlfrescoRenditionDefinition>

    fun getByRenditionName(renditionName: String): PromenaAlfrescoRenditionDefinition

    fun getByNodeName(nodeName: String): PromenaAlfrescoRenditionDefinition
}