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
    ): ExternalCommunication {
        val externalCommunicationId = properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.communication.external.id")

        logger.info { "Promena external communication: $externalCommunicationId" }

        return when (externalCommunicationId) {
            Memory -> ExternalCommunication(externalCommunicationId, null)
            File   -> ExternalCommunication(externalCommunicationId, properties.getLocation())
            else   -> throw UnsupportedOperationException("External communication must be <$Memory> or <$File>")
        }
    }
}