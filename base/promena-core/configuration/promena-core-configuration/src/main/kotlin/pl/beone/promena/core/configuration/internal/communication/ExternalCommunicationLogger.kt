package pl.beone.promena.core.configuration.internal.communication

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import javax.annotation.PostConstruct

@Configuration
class ExternalCommunicationLogger(
    private val externalCommunications: List<ExternalCommunication>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalCommunicationLogger::class.java)
    }

    @PostConstruct
    private fun log() {
        logger.info("Found <{}> external communication(s)", externalCommunications.size)
        externalCommunications.forEach { (id, incomingExternalCommunicationConverter, outgoingExternalCommunicationConverter) ->
            logger.info(
                "> Registered <{}> <{}, {}>",
                id,
                incomingExternalCommunicationConverter.javaClass.simpleName,
                outgoingExternalCommunicationConverter.javaClass.simpleName
            )
        }
    }
}