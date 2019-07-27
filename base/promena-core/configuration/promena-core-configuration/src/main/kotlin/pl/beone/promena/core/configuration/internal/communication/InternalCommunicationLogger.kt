package pl.beone.promena.core.configuration.internal.communication

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import javax.annotation.PostConstruct

@Configuration
class InternalCommunicationLogger(
    private val internalCommunicationConverter: InternalCommunicationConverter,
    @Qualifier("internalCommunicationParameters") private val communicationParameters: CommunicationParameters
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InternalCommunicationLogger::class.java)
    }

    @PostConstruct
    private fun log() {
        logger.info(
            "Internal communication: <{}> <{}> <{}>",
            communicationParameters.getId(),
            internalCommunicationConverter::class.qualifiedName,
            communicationParameters
        )
    }
}