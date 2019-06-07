package pl.beone.promena.connector.messagebroker.delivery.jms

import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.internal.communication.MapCommunicationParameters

internal class CommunicationParametersConverter {

    companion object {
        private const val PROMENA_COMMUNICATION_PREFIX = "promena_com_"
    }

    fun convert(headers: Map<String, Any>): CommunicationParameters =
            MapCommunicationParameters(
                    headers.filter { (key, _) -> key.startsWith(PROMENA_COMMUNICATION_PREFIX) }
                            .map { (key, value) -> key.removePrefix(PROMENA_COMMUNICATION_PREFIX) to value }
                            .toMap()
            )
}