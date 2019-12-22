package pl.beone.promena.alfresco.lib.rendition.configuration.external.definition

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.alfresco.lib.rendition.external.definition.MemoryPromenaRenditionDefinitionGetter

@Configuration
class MemoryPromenaRenditionDefinitionGetterContext {

    @Bean
    fun memoryPromenaRenditionDefinitionGetter(
        promenaRenditionDefinitions: List<PromenaRenditionDefinition>
    ) =
        MemoryPromenaRenditionDefinitionGetter(
            promenaRenditionDefinitions
        )
}