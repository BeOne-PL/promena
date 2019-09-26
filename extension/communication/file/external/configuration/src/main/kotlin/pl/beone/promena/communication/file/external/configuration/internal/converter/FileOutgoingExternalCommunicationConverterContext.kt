package pl.beone.promena.communication.file.external.configuration.internal.converter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.external.internal.converter.FileOutgoingExternalCommunicationConverter

@Configuration
class FileOutgoingExternalCommunicationConverterContext {

    @Bean
    fun fileOutgoingExternalCommunicationConverter() =
        FileOutgoingExternalCommunicationConverter
}