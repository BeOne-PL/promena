package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

class MapCommunicationParametersBuilder {

    private val parameters = HashMap<String, Any>()

    fun id(value: Any): MapCommunicationParametersBuilder =
        apply { parameters[CommunicationParameters.ID] = value }

    fun add(key: String, value: Any): MapCommunicationParametersBuilder =
        apply { parameters[key] = value }

    fun build(): MapCommunicationParameters =
        MapCommunicationParameters.of(
            parameters[CommunicationParameters.ID]?.toString()
                ?: throw IllegalArgumentException("Communication parameters must contain <${CommunicationParameters.ID}>"),
            parameters
        )

}