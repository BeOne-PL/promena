package pl.beone.promena.connector.http.contract.communication

import pl.beone.promena.core.contract.communication.CommunicationParameters

interface FromMapToCommunicationParametersConverter {

    fun convert(parameters: Map<String, String>): CommunicationParameters
}