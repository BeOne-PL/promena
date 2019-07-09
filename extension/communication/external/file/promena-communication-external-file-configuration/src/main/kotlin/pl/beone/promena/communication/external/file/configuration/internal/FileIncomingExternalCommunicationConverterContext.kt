package pl.beone.promena.communication.external.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.file.internal.FileIncomingExternalCommunicationConverter

@Configuration
class FileIncomingExternalCommunicationConverterContext {

    @Bean
    fun fileIncomingExternalCommunicationConverter() =
            FileIncomingExternalCommunicationConverter()
}