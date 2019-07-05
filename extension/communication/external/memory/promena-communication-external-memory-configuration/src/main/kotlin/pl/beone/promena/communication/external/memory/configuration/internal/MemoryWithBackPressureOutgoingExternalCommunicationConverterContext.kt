package pl.beone.promena.communication.external.memory.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.memory.internal.MemoryWithBackPressureOutgoingExternalCommunicationConverter

@Configuration
class MemoryWithBackPressureOutgoingExternalCommunicationConverterContext {

    @Bean
    fun memoryWithBackPressureOutgoingExternalCommunicationConverter() =
            MemoryWithBackPressureOutgoingExternalCommunicationConverter()
}