package pl.beone.promena.alfresco.module.rendition.external

import mu.KotlinLogging
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter

class MemoryAlfrescoPromenaRenditionDefinitionGetter(
    private val alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
) : AlfrescoPromenaRenditionDefinitionGetter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        logger.info { "Found <${alfrescoPromenaRenditionDefinitions.size}> rendition definitions" }
        alfrescoPromenaRenditionDefinitions.forEach {
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
        alfrescoPromenaRenditionDefinitions.map { it.getRenditionName() to it }.toMap()

    override fun getAll(): List<AlfrescoPromenaRenditionDefinition> =
        alfrescoPromenaRenditionDefinitions

    override fun getByRenditionName(renditionName: String): AlfrescoPromenaRenditionDefinition =
        renditionNameToDefinitionMap[renditionName] ?: throw PromenaNoSuchRenditionDefinitionException(renditionName, alfrescoPromenaRenditionDefinitions)
}