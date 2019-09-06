package pl.beone.promena.connector.activemq.delivery.jms

import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.core.applicationmodel.exception.communication.CommunicationParametersValidationException
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.communicationParameters

internal class CommunicationParametersConverter {

    fun convert(headers: Map<String, Any>): CommunicationParameters =
        communicationParameters(
            headers.getCommunicationId(),
            headers.filter { (key) -> key.startsWith(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_PREFIX) }
                .map { (key, value) -> key.removePrefix(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_PREFIX) to value }
                .toMap()
        )

    private fun Map<String, Any>.getCommunicationId(): String =
        get(PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID)?.toString()
            ?: throw CommunicationParametersValidationException("Headers must contain <${PromenaJmsHeaders.COMMUNICATION_PARAMETERS_ID}>")

}
