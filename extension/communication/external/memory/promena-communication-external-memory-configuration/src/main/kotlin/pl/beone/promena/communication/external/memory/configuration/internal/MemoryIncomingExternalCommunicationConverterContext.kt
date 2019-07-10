package pl.beone.promena.communication.external.memory.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.memory.internal.MemoryIncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter

@Configuration
class MemoryIncomingExternalCommunicationConverterContext {

    @Bean
    fun memoryIncomingExternalCommunicationConverter(internalCommunicationConverter: InternalCommunicationConverter) =
            MemoryIncomingExternalCommunicationConverter(internalCommunicationConverter)
}