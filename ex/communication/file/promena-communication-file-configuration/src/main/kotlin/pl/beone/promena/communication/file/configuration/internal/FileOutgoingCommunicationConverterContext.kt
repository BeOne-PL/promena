package pl.beone.promena.communication.file.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.FileOutgoingCommunicationConverter

@Configuration
class FileOutgoingCommunicationConverterContext {

    @Bean
    fun fileOutgoingCommunicationConverter() =
            FileOutgoingCommunicationConverter()
}