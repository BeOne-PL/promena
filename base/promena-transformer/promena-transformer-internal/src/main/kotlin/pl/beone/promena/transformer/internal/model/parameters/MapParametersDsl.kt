package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.Parameters.Companion.Timeout
import java.time.Duration

fun emptyParameters(): MapParameters =
        parameters(emptyMap())

fun parameters(parameters: Map<String, Any>): MapParameters =
        MapParameters.of(parameters)

operator fun Parameters.plus(entry: Pair<String, Any>): MapParameters =
        MapParameters.of(getAll() + entry)

infix fun Parameters.addTimeout(timeout: Duration): MapParameters =
        MapParameters.of(getAll() + (Timeout to timeout))