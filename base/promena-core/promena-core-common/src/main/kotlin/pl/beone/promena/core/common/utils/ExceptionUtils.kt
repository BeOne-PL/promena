package pl.beone.promena.core.common.utils

import java.util.concurrent.ExecutionException

fun <T> unwrapExecutionException(toExecute: () -> T): T =
        try {
            toExecute()
        } catch (e: ExecutionException) {
            if (e.cause != null) throw e.cause!! else throw e
        }

