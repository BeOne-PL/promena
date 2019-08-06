package pl.beone.promena.communication.memory.external.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.memory.internal.MemoryIncomingExternalCommunicationConverter

@Configuration
class MemoryIncomingExternalCommunicationConverterContext {

    @Bean
    fun memoryIncomingExternalCommunicationConverter() =
        MemoryIncomingExternalCommunicationConverter()
}