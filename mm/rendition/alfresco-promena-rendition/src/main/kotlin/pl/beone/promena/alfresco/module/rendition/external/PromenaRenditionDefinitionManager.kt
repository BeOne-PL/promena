package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.extension.getTransformationNodeName

class PromenaRenditionDefinitionManager(
    private val promenaAlfrescoRenditionDefinitions: List<PromenaAlfrescoRenditionDefinition>
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        logger.info { "Found <${promenaAlfrescoRenditionDefinitions.size}> rendition definitions" }
        promenaAlfrescoRenditionDefinitions.forEach {
            if (logger.isDebugEnabled) {
                logger.debug {
                    "> Registered rendition definition <${it::class.java.canonicalName} (${it.getRenditionName()}) [${it.getTransformation()}]>"
                }
            } else {
                logger.info {
                    "> Registered rendition definition <${it::class.java.canonicalName} (${it.getRenditionName()})>"
                }
            }
        }
    }

    private val renditionNameToDefinitionMap =
        promenaAlfrescoRenditionDefinitions.map { it.getRenditionName() to it }.toMap()

    private val nodeNameToDefinitionMap =
        promenaAlfrescoRenditionDefinitions.map { it.getTransformationNodeName() to it }.toMap()

    fun getAll(): List<PromenaAlfrescoRenditionDefinition> =
        promenaAlfrescoRenditionDefinitions

    fun getByRenditionName(renditionName: String): PromenaAlfrescoRenditionDefinition =
        renditionNameToDefinitionMap[renditionName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for <$renditionName> rendition isn't available. Available renditions: <[${createExceptionString { it.getRenditionName() }}]>",
                promenaAlfrescoRenditionDefinitions
            )

    fun getByNodeName(nodeName: String): PromenaAlfrescoRenditionDefinition =
        nodeNameToDefinitionMap[nodeName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for <$nodeName> node name isn't available. " +
                        "Available renditions: <[${createExceptionString(PromenaAlfrescoRenditionDefinition::getTransformationNodeName)}]>",
                promenaAlfrescoRenditionDefinitions
            )

    private fun createExceptionString(getName: (PromenaAlfrescoRenditionDefinition) -> String): String =
        promenaAlfrescoRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${getName(it)})" }
}