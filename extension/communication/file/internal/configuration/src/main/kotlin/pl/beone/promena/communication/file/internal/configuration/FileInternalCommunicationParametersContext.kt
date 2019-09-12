package pl.beone.promena.communication.file.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.file.model.internal.fileCommunicationParameters
import java.io.File

@Configuration
class FileInternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(
        environment: Environment
    ) =
        fileCommunicationParameters(
            File(environment.getRequiredProperty("communication.file.internal.directory.path"))
                .also { validate(it) }
        )

    private fun validate(directory: File) {
        require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't a directory" }
    }
}