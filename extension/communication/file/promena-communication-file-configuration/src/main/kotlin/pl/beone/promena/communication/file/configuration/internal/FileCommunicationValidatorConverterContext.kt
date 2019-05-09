package pl.beone.promena.communication.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.FileCommunicationValidatorConverter

@Configuration
class FileCommunicationValidatorConverterContext {

    @Bean
    fun fileCommunicationValidatorConverter() =
            FileCommunicationValidatorConverter()
}