package pl.beone.promena.communication.file.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.model.internal.FileCommunicationWritableDataCreator

@Configuration
class FileInternalCommunicationWritableDataCreatorContext {

    @Bean
    fun internalCommunicationWritableDataCreator() =
        FileCommunicationWritableDataCreator
}