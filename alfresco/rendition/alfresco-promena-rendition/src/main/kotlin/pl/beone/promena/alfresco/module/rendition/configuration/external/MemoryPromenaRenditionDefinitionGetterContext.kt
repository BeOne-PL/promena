package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.external.MemoryPromenaRenditionDefinitionGetter

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