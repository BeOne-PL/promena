package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.communication.CommunicationParameters.Companion.Id

class MapCommunicationParametersBuilder {

    private val parameters = HashMap<String, Any>()

    fun id(value: Any): MapCommunicationParametersBuilder =
            apply { parameters[Id] = value }

    fun add(key: String, value: Any): MapCommunicationParametersBuilder =
            apply { parameters[key] = value }

    fun build(): MapCommunicationParameters =
            if (parameters.containsKey(Id)) {
                MapCommunicationParameters(parameters)
            } else {
                throw IllegalStateException("Communication parameters has to contain <id>")
            }

}