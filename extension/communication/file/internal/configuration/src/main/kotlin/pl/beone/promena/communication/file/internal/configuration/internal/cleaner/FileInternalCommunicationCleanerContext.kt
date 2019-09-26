package pl.beone.promena.communication.file.internal.configuration.internal.cleaner

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.file.internal.internal.cleaner.FileInternalCommunicationCleaner

@Configuration
class FileInternalCommunicationCleanerContext {

    @Bean
    fun fileInternalCommunicationCleaner() =
        FileInternalCommunicationCleaner
}