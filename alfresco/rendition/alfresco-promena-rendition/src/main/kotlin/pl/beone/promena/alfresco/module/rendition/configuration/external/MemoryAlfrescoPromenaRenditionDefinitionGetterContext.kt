package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.external.MemoryAlfrescoPromenaRenditionDefinitionGetter

@Configuration
class MemoryAlfrescoPromenaRenditionDefinitionGetterContext {

    @Bean
    fun memoryAlfrescoPromenaRenditionDefinitionGetter(
        alfrescoPromenaRenditionDefinitions: List<AlfrescoPromenaRenditionDefinition>
    ) =
        MemoryAlfrescoPromenaRenditionDefinitionGetter(
            alfrescoPromenaRenditionDefinitions
        )
}