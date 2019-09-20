package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.internal.MemoryAlfrescoPromenaRenditionInProgressManager

@Configuration
class MemoryAlfrescoPromenaRenditionInProgressManagerContext {

    @Bean
    fun memoryAlfrescoPromenaRenditionInProgressManager() =
        MemoryAlfrescoPromenaRenditionInProgressManager()

}