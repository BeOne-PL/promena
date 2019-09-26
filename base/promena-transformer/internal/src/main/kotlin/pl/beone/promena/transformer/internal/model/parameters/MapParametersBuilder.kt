package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.Parameters.Companion.TIMEOUT
import java.time.Duration

class MapParametersBuilder {

    private val parameters = HashMap<String, Any>()

    fun add(key: String, value: Any): MapParametersBuilder =
        apply { parameters[key] = value }

    fun addTimeout(timeout: Duration): MapParametersBuilder =
        apply { parameters[TIMEOUT] = timeout }

    fun build(): MapParameters =
        parameters(parameters)
}