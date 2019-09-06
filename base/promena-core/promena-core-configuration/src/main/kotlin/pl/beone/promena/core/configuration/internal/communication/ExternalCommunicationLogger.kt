package pl.beone.promena.core.configuration.internal.communication

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import javax.annotation.PostConstruct

@Configuration
class ExternalCommunicationLogger(
    private val externalCommunications: List<ExternalCommunication>
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info { "Found <${externalCommunications.size}> external communication(s)" }
        externalCommunications.forEach { (id, incomingExternalCommunicationConverter, outgoingExternalCommunicationConverter) ->
            logger.info {
                "> Registered <$id> <${incomingExternalCommunicationConverter.javaClass.canonicalName}, ${outgoingExternalCommunicationConverter.javaClass.canonicalName}>"
            }
        }
    }
}