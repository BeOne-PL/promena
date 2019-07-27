package pl.beone.promena.core.external.akka.util

import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

internal fun Parameters.getTimeoutOrInfiniteIfNotFound(addToTimeout: Duration = Duration.ZERO): Duration =
    try {
        this.getTimeout().plus(addToTimeout)
    } catch (e: Exception) {
        infiniteDuration
    }