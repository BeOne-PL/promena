package pl.beone.promena.alfresco.module.client.base.common

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
    if (this != null) {
        prettyFormatter.print(org.joda.time.Duration.millis(this.toMillis()).toPeriod())
    } else {
        "infinite"
    }