package pl.beone.promena.communication.memory.external.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.memory.external.internal.MemoryIncomingExternalCommunicationConverter
import pl.beone.promena.communication.memory.external.internal.MemoryOutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication

@Configuration
class MemoryExternalCommunicationContext {

    @Bean
    fun memoryExternalCommunication(
        environment: Environment,
        memoryIncomingExternalCommunicationConverter: MemoryIncomingExternalCommunicationConverter,
        memoryOutgoingExternalCommunicationConverter: MemoryOutgoingExternalCommunicationConverter
    ) =
        ExternalCommunication(
            environment.getCommunicationId(),
            memoryIncomingExternalCommunicationConverter,
            memoryOutgoingExternalCommunicationConverter
        )

    private fun Environment.getCommunicationId(): String =
        getRequiredProperty("communication.memory.external.id")
}