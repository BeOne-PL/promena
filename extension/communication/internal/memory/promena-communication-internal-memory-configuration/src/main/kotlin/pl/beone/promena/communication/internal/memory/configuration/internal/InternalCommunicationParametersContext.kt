package pl.beone.promena.communication.internal.memory.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters

@Configuration
class InternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(environment: Environment) =
            MapCommunicationParameters.create(environment.getCommunicationId())

    private fun Environment.getCommunicationId(): String =
            getRequiredProperty("communication.internal.memory.id")
}