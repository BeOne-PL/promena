package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters.Companion.ID

class MapCommunicationParametersBuilder {

    private val parameters = HashMap<String, Any>()

    fun id(value: Any): MapCommunicationParametersBuilder =
            apply { parameters[ID] = value }

    fun add(key: String, value: Any): MapCommunicationParametersBuilder =
            apply { parameters[key] = value }

    fun build(): MapCommunicationParameters =
            if (parameters.containsKey(ID)) {
                MapCommunicationParameters(parameters)
            } else {
                throw IllegalStateException("Communication parameters has to contain <id>")
            }

}