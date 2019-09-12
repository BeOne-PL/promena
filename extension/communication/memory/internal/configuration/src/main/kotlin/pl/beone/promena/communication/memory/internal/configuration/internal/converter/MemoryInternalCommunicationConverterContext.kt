package pl.beone.promena.communication.memory.internal.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.internal.internal.converter.MemoryInternalCommunicationConverter

@Configuration
class MemoryInternalCommunicationConverterContext {

    @Bean
    fun memoryInternalCommunicationConverter() =
        MemoryInternalCommunicationConverter()
}