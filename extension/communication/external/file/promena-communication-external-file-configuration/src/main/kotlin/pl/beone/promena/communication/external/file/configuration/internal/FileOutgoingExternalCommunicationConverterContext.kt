package pl.beone.promena.communication.external.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.external.file.internal.FileOutgoingExternalCommunicationConverter

@Configuration
class FileOutgoingExternalCommunicationConverterContext {

    @Bean
    fun fileOutgoingExternalCommunicationConverter() =
            FileOutgoingExternalCommunicationConverter()
}