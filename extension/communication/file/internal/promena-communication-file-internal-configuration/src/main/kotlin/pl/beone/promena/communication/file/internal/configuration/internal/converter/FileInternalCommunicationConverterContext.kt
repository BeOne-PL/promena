package pl.beone.promena.communication.file.internal.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.internal.configuration.extension.getLocation
import pl.beone.promena.communication.file.internal.internal.converter.FileInternalCommunicationConverter

@Configuration
class FileInternalCommunicationConverterContext {

    @Bean
    fun fileInternalCommunicationConverter(
        environment: Environment
    ) =
        FileInternalCommunicationConverter(
            environment.getLocation()
        )
}