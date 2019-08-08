package pl.beone.promena.communication.file.internal.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.FileInternalCommunicationCleaner

@Configuration
class FileInternalCommunicationCleanerContext {

    @Bean
    fun fileInternalCommunicationCleaner() =
        FileInternalCommunicationCleaner()
}