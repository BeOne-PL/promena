package pl.beone.promena.core.external.akka.util

import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

internal fun Parameters.getTimeoutOrInfiniteIfNotFound(): Duration =
    try {
        this.getTimeout()
    } catch (e: Exception) {
        infiniteDuration
    }