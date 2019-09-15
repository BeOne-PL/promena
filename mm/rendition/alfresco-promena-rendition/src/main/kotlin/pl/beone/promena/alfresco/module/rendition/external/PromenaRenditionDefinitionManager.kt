package pl.beone.promena.alfresco.module.rendition.external

import pl.beone.promena.alfresco.module.client.base.util.createNodeName
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition

class PromenaRenditionDefinitionManager(
    private val promenaAlfrescoRenditionDefinitions: List<PromenaAlfrescoRenditionDefinition>
) {

    private val renditionNameToDefinitionMap =
        promenaAlfrescoRenditionDefinitions.map { it.getRenditionName() to it }.toMap()
    private val nodeNameToDefinitionMap =
        promenaAlfrescoRenditionDefinitions.map { it.getTransformation().createNodeName() to it }.toMap()

    fun getAll(): List<PromenaAlfrescoRenditionDefinition> =
        promenaAlfrescoRenditionDefinitions

    fun getByRenditionName(renditionName: String): PromenaAlfrescoRenditionDefinition =
        renditionNameToDefinitionMap[renditionName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for rendition name <$renditionName> isn't available. Available renditions: <[${createExceptionString { it.getRenditionName() }}]>",
                promenaAlfrescoRenditionDefinitions
            )

    fun getByNodeName(nodeName: String): PromenaAlfrescoRenditionDefinition =
        nodeNameToDefinitionMap[nodeName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for node name <$nodeName> isn't available. " +
                        "Available renditions: <[${createExceptionString { it.getTransformation().createNodeName() }}]>",
                promenaAlfrescoRenditionDefinitions
            )

    private fun createExceptionString(getName: (PromenaAlfrescoRenditionDefinition) -> String): String =
        promenaAlfrescoRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${getName(it)})" }
}