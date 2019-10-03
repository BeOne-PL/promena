package pl.beone.promena.alfresco.module.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.rendition.internal.MemoryAlfrescoPromenaRenditionInProgressSynchronizer

@Configuration
class MemoryAlfrescoPromenaRenditionInProgressSynchronizerContext {

    @Bean
    fun memoryAlfrescoPromenaRenditionInProgressSynchronizer() =
        MemoryAlfrescoPromenaRenditionInProgressSynchronizer()

}