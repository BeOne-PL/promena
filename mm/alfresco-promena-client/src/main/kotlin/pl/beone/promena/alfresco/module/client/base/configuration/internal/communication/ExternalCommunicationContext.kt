package pl.beone.promena.alfresco.module.client.base.configuration.internal.communication

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.Memory
import pl.beone.promena.alfresco.module.client.base.extension.getRequiredPropertyWithResolvedPlaceholders
import java.io.IOException
import java.net.URI
import java.util.*

@Configuration
class ExternalCommunicationContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    fun externalCommunication(
        @Qualifier("global-properties") properties: Properties
    ): ExternalCommunication =
        when (val externalCommunicationId = properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.communication.external.id")) {
            Memory -> {
                logger.info { "Promena external communication: <memory>" }
                ExternalCommunication(externalCommunicationId, null)
            }
            File -> {
                val location =
                    determineLocation(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.communication.external.file.location"))
                logger.info { "Promena external communication: <file, location: $location>" }
                ExternalCommunication(externalCommunicationId, location)
            }
            else -> throw IllegalStateException("External communication must be <$Memory> or <$File>")
        }

    fun determineLocation(location: String): URI =
        try {
            URI(location)
                .also { validate(it) }
        } catch (e: Exception) {
            throw IllegalArgumentException("Communication location <$location> isn't correct", e)
        }

    private fun validate(uri: URI) {
        val file = java.io.File(uri)

        if (!file.exists()) {
            throw IOException("Path <$uri> doesn't exist")
        }

        if (file.isFile) {
            throw IOException("Path <$uri> is a file but should be a directory")
        }
    }
}