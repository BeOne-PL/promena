package pl.beone.promena.core.external.akka.extension

import akka.util.Timeout
import org.joda.time.format.PeriodFormatterBuilder
import java.time.Duration

private val prettyFormatter = PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix("d")
    .appendSeparator(" ")
    .appendHours()
    .appendSuffix("h")
    .appendSeparator(" ")
    .appendMinutes()
    .appendSuffix("m")
    .appendSeparator(" ")
    .appendSeconds()
    .appendSuffix("s")
    .appendSeparator(" ")
    .appendMillis()
    .appendSuffix("ms")
    .appendSeparator(" ")
    .toFormatter()

fun Duration?.toPrettyString(): String =
    if (this != null) prettyFormatter.print(org.joda.time.Duration.millis(this.toMillis()).toPeriod()) else "infinite"

operator fun Duration.plus(duration: Duration): Duration =
    this.plus(duration)

fun Duration.toTimeout(): Timeout =
    Timeout.create(this)