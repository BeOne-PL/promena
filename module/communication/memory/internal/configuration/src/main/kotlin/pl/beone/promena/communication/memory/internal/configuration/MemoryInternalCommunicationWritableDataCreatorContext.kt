package pl.beone.promena.communication.memory.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.model.internal.MemoryCommunicationWritableDataCreator

@Configuration
class MemoryInternalCommunicationWritableDataCreatorContext {

    @Bean
    fun internalCommunicationWritableDataCreator() =
        MemoryCommunicationWritableDataCreator
}