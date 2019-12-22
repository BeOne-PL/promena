package pl.beone.promena.alfresco.lib.rendition.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.lib.rendition.internal.MemoryPromenaRenditionInProgressSynchronizer

@Configuration
class MemoryPromenaRenditionInProgressSynchronizerContext {

    @Bean
    fun memoryPromenaRenditionInProgressSynchronizer() =
        MemoryPromenaRenditionInProgressSynchronizer()
}