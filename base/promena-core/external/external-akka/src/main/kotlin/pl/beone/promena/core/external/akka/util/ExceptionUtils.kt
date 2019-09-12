package pl.beone.promena.core.external.akka.util

import java.util.concurrent.ExecutionException

internal fun <T> unwrapExecutionException(toExecute: () -> T): T =
    try {
        toExecute()
    } catch (e: ExecutionException) {
        if (e.cause != null) throw e.cause!! else throw e
    }

