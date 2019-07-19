package pl.beone.promena.transformer.internal.model.parameters

import pl.beone.promena.transformer.contract.model.Parameters.Companion.Timeout
import java.time.Duration

fun emptyParameters(): MapParameters =
        parameters()

fun parameters(parameters: Map<String, Any> = emptyMap()): MapParameters =
        MapParameters.of(parameters)

infix fun MapParameters.add(entry: Pair<String, Any>): MapParameters =
        MapParameters.of(getAll() + entry)

infix fun MapParameters.addTimeout(timeout: Duration): MapParameters =
        MapParameters.of(getAll() + (Timeout to timeout))