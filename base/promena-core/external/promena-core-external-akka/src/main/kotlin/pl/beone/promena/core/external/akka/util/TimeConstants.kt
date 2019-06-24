package pl.beone.promena.core.external.akka.util

import akka.util.Timeout
import java.time.Duration

internal const val infinite = 21474835000

internal val infiniteDuration = Duration.ofSeconds(infinite / 1000)!!

internal val infiniteTimeout = Timeout.create(infiniteDuration)
