@file:JvmName("MapParametersDsl")

package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

fun emptyParameters(): MapParameters =
    parameters(emptyMap())

fun parameters(parameters: Map<String, Any>): MapParameters =
    MapParameters.of(parameters)

operator fun Parameters.plus(entry: Pair<String, Any>): MapParameters =
    MapParameters.of(getAll() + entry)

infix fun Parameters.addIfNotNull(entry: Pair<String, Any?>): MapParameters {
    val (key, value) = entry
    return if (value != null) {
        MapParameters.of(getAll() + (key to value))
    } else {
        MapParameters.of(this.getAll())
    }
}

infix fun Parameters.addTimeout(timeout: Duration): MapParameters =
    MapParameters.of(getAll() + (Parameters.TIMEOUT to timeout))

operator fun Parameters.plus(parameters: Parameters): MapParameters =
    MapParameters.of(getAll() + parameters.getAll())