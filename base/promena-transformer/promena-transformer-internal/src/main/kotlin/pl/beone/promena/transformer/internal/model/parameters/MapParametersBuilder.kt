package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters.Companion.Timeout
import java.time.Duration

data class MapParametersBuilder internal constructor(private val parameters: MutableMap<String, Any> = HashMap()) {

    fun parameter(key: String, value: Any): MapParametersBuilder =
            apply { parameters[key] = value }

    fun timeout(timeout: Duration): MapParametersBuilder =
            apply { parameters[Timeout] = timeout }

    fun build(): MapParameters = MapParameters(parameters)

}