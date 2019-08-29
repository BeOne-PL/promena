package pl.beone.promena.communication.memory.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.memory.internal.configuration.extension.getId
import pl.beone.promena.transformer.internal.communication.communicationParameters

@Configuration
class MemoryInternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(
        environment: Environment
    ) =
        communicationParameters(environment.getId())
}