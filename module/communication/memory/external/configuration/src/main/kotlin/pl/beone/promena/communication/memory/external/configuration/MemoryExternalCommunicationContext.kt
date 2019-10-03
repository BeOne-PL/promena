package pl.beone.promena.communication.memory.external.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.external.internal.converter.MemoryIncomingExternalCommunicationConverter
import pl.beone.promena.communication.memory.external.internal.converter.MemoryOutgoingExternalCommunicationConverter
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

@Configuration
class MemoryExternalCommunicationContext {

    @Bean
    fun memoryExternalCommunication(
        memoryIncomingExternalCommunicationConverter: MemoryIncomingExternalCommunicationConverter,
        memoryOutgoingExternalCommunicationConverter: MemoryOutgoingExternalCommunicationConverter
    ) =
        ExternalCommunication(
            MemoryCommunicationParameters.ID,
            memoryIncomingExternalCommunicationConverter,
            memoryOutgoingExternalCommunicationConverter
        )
}