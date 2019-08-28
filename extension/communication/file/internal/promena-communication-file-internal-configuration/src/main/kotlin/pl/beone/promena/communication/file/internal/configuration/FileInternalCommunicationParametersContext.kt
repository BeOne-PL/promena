package pl.beone.promena.communication.file.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.internal.configuration.extension.getId
import pl.beone.promena.communication.file.internal.configuration.extension.getLocation
import pl.beone.promena.transformer.internal.communication.communicationParameters
import pl.beone.promena.transformer.internal.communication.plus

@Configuration
class FileInternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(
        environment: Environment
    ) =
        communicationParameters(environment.getId()) +
                ("location" to environment.getLocation())
}