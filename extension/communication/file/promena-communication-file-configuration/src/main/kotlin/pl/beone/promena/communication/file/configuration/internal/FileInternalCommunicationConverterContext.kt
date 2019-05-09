package pl.beone.promena.communication.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.internal.FileInternalCommunicationConverter

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(environment: Environment) =
            FileInternalCommunicationConverter(environment.getLocationAndVerify())

}