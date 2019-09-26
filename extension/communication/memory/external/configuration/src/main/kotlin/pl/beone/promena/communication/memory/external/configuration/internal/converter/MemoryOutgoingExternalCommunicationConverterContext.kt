package pl.beone.promena.communication.memory.external.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.external.internal.converter.MemoryOutgoingExternalCommunicationConverter

@Configuration
class MemoryOutgoingExternalCommunicationConverterContext {

    @Bean
    fun memoryOutgoingExternalCommunicationConverter() =
        MemoryOutgoingExternalCommunicationConverter
}