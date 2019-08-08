package pl.beone.promena.core.configuration.internal.communication

import mu.KotlinLogging
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
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Internal communication <${communicationParameters.getId()}> <${internalCommunicationConverter::class.qualifiedName}> <${internalCommunicationConverter::class.qualifiedName}>"
        }
    }
}