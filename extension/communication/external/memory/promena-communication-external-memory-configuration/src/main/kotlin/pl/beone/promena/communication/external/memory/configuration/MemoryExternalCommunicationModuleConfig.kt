package pl.beone.promena.communication.external.memory.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import pl.beone.promena.communication.external.memory.internal.MemoryWithBackPressureIncomingExternalCommunicationConverter
import pl.beone.promena.communication.external.memory.internal.MemoryWithBackPressureOutgoingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.internal.communication.MapCommunicationParameters

@Configuration
@PropertySource("classpath:module-communication-external-memory.properties")
class MemoryExternalCommunicationModuleConfig {

    @Bean
    fun internalCommunicationParameters(environment: Environment) =
            MapCommunicationParameters.create(environment.getCommunicationId())

    @Bean
    fun memoryExternalCommunication(environment: Environment,
                                    memoryWithBackPressureIncomingExternalCommunicationConverter: MemoryWithBackPressureIncomingExternalCommunicationConverter,
                                    memoryWithBackPressureOutgoingExternalCommunicationConverter: MemoryWithBackPressureOutgoingExternalCommunicationConverter) =
            ExternalCommunication(environment.getCommunicationId(),
                                  memoryWithBackPressureIncomingExternalCommunicationConverter,
                                  memoryWithBackPressureOutgoingExternalCommunicationConverter)

    private fun Environment.getCommunicationId(): String =
            getRequiredProperty("communication.external.memory.id")
}