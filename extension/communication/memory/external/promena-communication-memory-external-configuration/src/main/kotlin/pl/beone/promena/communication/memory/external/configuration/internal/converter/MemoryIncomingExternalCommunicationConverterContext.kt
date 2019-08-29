package pl.beone.promena.communication.memory.external.configuration.internal.converter

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.memory.external.configuration.extension.getId
import pl.beone.promena.communication.memory.external.internal.converter.MemoryIncomingExternalCommunicationConverter
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class MemoryIncomingExternalCommunicationConverterContext {

    @Bean
    fun memoryIncomingExternalCommunicationConverter(
        environment: Environment,
        @Qualifier("internalCommunicationParameters") internalCommunicationParameters: CommunicationParameters,
        internalCommunicationConverter: InternalCommunicationConverter
    ) =
        MemoryIncomingExternalCommunicationConverter(
            environment.getId(),
            internalCommunicationParameters.getId(),
            internalCommunicationConverter
        )
}