package pl.beone.promena.communication.memory.internal.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.internal.memory.internal.MemoryInternalCommunicationConverter

@Configuration
class MemoryInternalCommunicationConverterContext {

    @Bean
    fun memoryInternalCommunicationConverter() =
        MemoryInternalCommunicationConverter()
}