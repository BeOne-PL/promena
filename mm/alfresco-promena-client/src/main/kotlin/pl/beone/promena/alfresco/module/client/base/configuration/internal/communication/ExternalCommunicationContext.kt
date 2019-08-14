package pl.beone.promena.alfresco.module.client.base.configuration.internal.communication

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunication
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.File
import pl.beone.promena.alfresco.module.client.base.applicationmodel.communication.ExternalCommunicationConstants.Memory
import pl.beone.promena.alfresco.module.client.base.configuration.getLocation
import pl.beone.promena.alfresco.module.client.base.configuration.getRequiredPropertyWithResolvedPlaceholders
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
            File   -> {
                val location = properties.getLocation()
                logger.info { "Promena external communication: <file, location: $location>" }
                ExternalCommunication(externalCommunicationId, location)
            }
            else   -> throw UnsupportedOperationException("External communication must be <$Memory> or <$File>")
        }
}