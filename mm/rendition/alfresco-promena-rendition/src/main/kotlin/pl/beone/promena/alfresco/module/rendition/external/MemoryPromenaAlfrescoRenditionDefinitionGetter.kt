package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getTransformationNodeName

class MemoryPromenaAlfrescoRenditionDefinitionGetter(
    private val promenaAlfrescoRenditionDefinitions: List<PromenaAlfrescoRenditionDefinition>
) : PromenaAlfrescoRenditionDefinitionGetter {

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

    override fun getAll(): List<PromenaAlfrescoRenditionDefinition> =
        promenaAlfrescoRenditionDefinitions

    override fun getByRenditionName(renditionName: String): PromenaAlfrescoRenditionDefinition =
        renditionNameToDefinitionMap[renditionName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for <$renditionName> rendition isn't available. " +
                        "Available renditions: <[${createExceptionString(PromenaAlfrescoRenditionDefinition::getRenditionName)}]>",
                promenaAlfrescoRenditionDefinitions
            )

    override fun getByNodeName(nodeName: String): PromenaAlfrescoRenditionDefinition =
        nodeNameToDefinitionMap[nodeName]
            ?: throw PromenaNoSuchRenditionDefinitionException(
                "Definition for <$nodeName> node name isn't available. " +
                        "Available renditions: <[${createExceptionString(PromenaAlfrescoRenditionDefinition::getTransformationNodeName)}]>",
                promenaAlfrescoRenditionDefinitions
            )

    private fun createExceptionString(getName: (PromenaAlfrescoRenditionDefinition) -> String): String =
        promenaAlfrescoRenditionDefinitions.joinToString(", ") { "${it::class.java.canonicalName}(${getName(it)})" }
}