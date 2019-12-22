package pl.beone.promena.alfresco.lib.rendition.external.definition

import mu.KotlinLogging
import pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception.NoSuchPromenaRenditionDefinitionException
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinitionGetter

class MemoryPromenaRenditionDefinitionGetter(
    private val promenaRenditionDefinitions: List<PromenaRenditionDefinition>
) : PromenaRenditionDefinitionGetter {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        logger.info { "Found <${promenaRenditionDefinitions.size}> rendition definitions" }
        promenaRenditionDefinitions.forEach {
            logger.info {
                "> Registered rendition definition <${it::class.java.canonicalName} (${it.getRenditionName()})>"
            }
        }
    }

    private val renditionNameToDefinitionMap =
        promenaRenditionDefinitions.map { it.getRenditionName() to it }.toMap()

    override fun getAll(): List<PromenaRenditionDefinition> =
        promenaRenditionDefinitions

    override fun getByRenditionName(renditionName: String): PromenaRenditionDefinition =
        renditionNameToDefinitionMap[renditionName]
            ?: throw NoSuchPromenaRenditionDefinitionException(renditionName, promenaRenditionDefinitions)
}