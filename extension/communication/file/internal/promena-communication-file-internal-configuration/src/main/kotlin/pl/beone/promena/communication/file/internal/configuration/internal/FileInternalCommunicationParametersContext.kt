package pl.beone.promena.communication.file.internal.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus
import java.net.URI

@Configuration
class FileInternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(
        environment: Environment
    ) =
        communicationParameters(environment.getRequiredProperty("communication.file.internal.id")) +
                ("location" to URI(environment.getRequiredProperty("communication.file.internal.location")))
}