package pl.beone.promena.alfresco.module.rendition.configuration.external

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.external.MemoryPromenaAlfrescoRenditionDefinitionGetter

@Configuration
class MemoryPromenaAlfrescoRenditionDefinitionGetterContext {

    @Bean
    fun memoryPromenaAlfrescoRenditionDefinitionGetter(promenaAlfrescoRenditionDefinitions: List<PromenaAlfrescoRenditionDefinition>) =
        MemoryPromenaAlfrescoRenditionDefinitionGetter(promenaAlfrescoRenditionDefinitions)
}