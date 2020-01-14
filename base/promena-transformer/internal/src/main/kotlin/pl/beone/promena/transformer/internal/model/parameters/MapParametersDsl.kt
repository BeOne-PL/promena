@file:JvmName("MapParametersDsl")

package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.Parameters.Companion.TIMEOUT
import java.time.Duration

fun emptyParameters(): MapParameters =
    parameters(emptyMap())

fun parameters(parameters: Map<String, Any>): MapParameters =
    MapParameters.of(parameters)

/**
 * ```
 * emptyParameters()
 *      + ("force" to true)
 * ```
 *
 * @return concatenation of `this` and [entry]
 */
operator fun Parameters.plus(entry: Pair<String, Any>): MapParameters =
    MapParameters.of(getAll() + entry)

/**
 * ```
 * emptyParameters() addIfNotNull
 *      ("force" to true) addIfNotNull
 *      ("includeMetadata" to null)
 * ```
 *
 * @return concatenation of `this` and [entry] if the value of [entry] isn't `null`
 */
infix fun Parameters.addIfNotNull(entry: Pair<String, Any?>): MapParameters {
    val (key, value) = entry
    return if (value != null) {
        MapParameters.of(getAll() + (key to value))
    } else {
        MapParameters.of(this.getAll())
    }
}

/**
 * ```
 * emptyParameters() addTimeout
 *      Duration.ofMinutes(10)
 * ```
 *
 * @return concatenation of `this` and *timeout*
 */
infix fun Parameters.addTimeout(timeout: Duration): MapParameters =
    MapParameters.of(getAll() + (TIMEOUT to timeout))

/**
 * ```
 * parameters(mapOf("force" to true)) +
 *      parameters(mapOf("includeMetadata" to false))
 * ```
 *
 * @return concatenation of `this` and [parameters]
 */
operator fun Parameters.plus(parameters: Parameters): MapParameters =
    MapParameters.of(getAll() + parameters.getAll())