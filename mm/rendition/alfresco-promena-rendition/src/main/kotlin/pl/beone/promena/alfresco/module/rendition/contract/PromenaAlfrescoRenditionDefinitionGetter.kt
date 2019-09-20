package pl.beone.promena.alfresco.module.rendition.contract

interface PromenaAlfrescoRenditionDefinitionGetter {

    fun getAll(): List<PromenaAlfrescoRenditionDefinition>

    fun getByRenditionName(renditionName: String): PromenaAlfrescoRenditionDefinition

    fun getByNodeName(nodeName: String): PromenaAlfrescoRenditionDefinition
}