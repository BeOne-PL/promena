package pl.beone.promena.transformer.internal.communication

import pl.beone.promena.transformer.contract.model.Parameters

data class MapCommunicationParametersBuilder internal constructor(private val parameters: MutableMap<String, Any> = HashMap()) {

    fun parameter(key: String, value: Any): MapCommunicationParametersBuilder =
            apply { parameters[key] = value }

    fun parameter(key: String, parameters: Parameters): MapCommunicationParametersBuilder =
            parameter(key, parameters as Any)

    fun build(): MapCommunicationParameters = MapCommunicationParameters(parameters)

}