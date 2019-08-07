package pl.beone.promena.communication.file.external.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.external.internal.FileOutgoingExternalCommunicationConverter

@Configuration
class FileOutgoingExternalCommunicationConverterContext {

    @Bean
    fun fileOutgoingExternalCommunicationConverter() =
        FileOutgoingExternalCommunicationConverter()
}