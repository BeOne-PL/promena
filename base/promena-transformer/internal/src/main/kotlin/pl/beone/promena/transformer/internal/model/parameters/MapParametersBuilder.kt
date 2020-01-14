package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters.Companion.TIMEOUT
import java.time.Duration

/**
 * Helps to construct [MapParameters].
 * Targeted at Java developers. If you are Kotlin developer, it's a better idea to use DSL.
 *
 * @see MapParametersDsl
 */
class MapParametersBuilder {

    private val parameters = HashMap<String, Any>()

    fun add(key: String, value: Any): MapParametersBuilder =
        apply { parameters[key] = value }

    fun addTimeout(timeout: Duration): MapParametersBuilder =
        apply { parameters[TIMEOUT] = timeout }

    fun build(): MapParameters =
        parameters(parameters)
}