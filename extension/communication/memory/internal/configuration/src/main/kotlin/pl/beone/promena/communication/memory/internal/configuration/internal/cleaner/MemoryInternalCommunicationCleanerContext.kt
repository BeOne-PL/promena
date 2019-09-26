package pl.beone.promena.communication.memory.internal.configuration.internal.cleaner

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.internal.internal.cleaner.MemoryInternalCommunicationCleaner

@Configuration
class MemoryInternalCommunicationCleanerContext {

    @Bean
    fun memoryInternalCommunicationCleaner() =
        MemoryInternalCommunicationCleaner
}