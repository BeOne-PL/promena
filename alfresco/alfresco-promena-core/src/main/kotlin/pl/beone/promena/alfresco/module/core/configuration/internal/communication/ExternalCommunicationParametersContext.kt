package pl.beone.promena.alfresco.module.core.configuration.internal.communication

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.extension.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.communication.file.model.internal.DefaultFileCommunicationParameters
import pl.beone.promena.communication.file.model.internal.fileCommunicationParameters
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import java.io.File
import java.util.*

@Configuration
class ExternalCommunicationParametersContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    fun externalCommunicationParameters(
        @Qualifier("global-properties") properties: Properties
    ): CommunicationParameters =
        when (properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.communication.external.id")) {
            MemoryCommunicationParameters.ID -> {
                logger.info { "Promena external communication: <${MemoryCommunicationParameters.ID}>" }
                memoryCommunicationParameters()
            }
            FileCommunicationParameters.ID -> {
                val directory =
                    determineDirectory(properties.getRequiredPropertyWithResolvedPlaceholders("promena.core.communication.external.file.directory.path"))
                logger.info { "Promena external communication: <${FileCommunicationParameters.ID}, ${DefaultFileCommunicationParameters.DIRECTORY}: ${directory}>" }
                fileCommunicationParameters(directory)
            }
            else ->
                error("External communication must be <${MemoryCommunicationParameters.ID}> or <${FileCommunicationParameters.ID}>")
        }

    fun determineDirectory(path: String): File =
        File(path)
            .also { require(it.exists() && it.isDirectory) { "Directory <$it> doesn't exist or isn't directory" } }
}