package pl.beone.promena.core.configuration.internal.communication

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import javax.annotation.PostConstruct

@Configuration
class InternalCommunicationLogger(private val internalCommunicationConverter: InternalCommunicationConverter) {

    companion object {
        private val logger = LoggerFactory.getLogger(InternalCommunicationLogger::class.java)
    }

    @PostConstruct
    private fun log() {
        logger.info("Internal communication: {}", internalCommunicationConverter::class.qualifiedName)
    }
}