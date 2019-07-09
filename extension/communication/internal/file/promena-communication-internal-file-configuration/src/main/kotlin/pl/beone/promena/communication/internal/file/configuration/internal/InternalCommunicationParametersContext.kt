package pl.beone.promena.communication.internal.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import java.net.URI

@Configuration
class InternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(environment: Environment) =
            MapCommunicationParameters.create(environment.getCommunicationId(),
                                              mapOf("location" to environment.getLocation()))

    private fun Environment.getCommunicationId(): String =
            getRequiredProperty("communication.internal.file.id")

    private fun Environment.getLocation(): URI =
            URI(getRequiredProperty("communication.internal.file.location"))
}