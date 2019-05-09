package pl.beone.promena.communication.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.internal.FileIncomingCommunicationConverter

@Configuration
class FileIncomingCommunicationConverterContext {

    @Bean
    fun fileIncomingCommunicationConverter(environment: Environment) =
            FileIncomingCommunicationConverter(environment.getLocationAndVerify())
}