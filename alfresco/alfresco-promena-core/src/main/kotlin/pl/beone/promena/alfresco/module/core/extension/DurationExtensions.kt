package pl.beone.promena.alfresco.module.core.extension

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

private val propertiesFormatter = PeriodFormatterBuilder()
    .appendDays().appendSuffix("d")
    .appendHours().appendSuffix("h")
    .appendMinutes().appendSuffix("m")
    .appendSeconds().appendSuffix("s")
    .appendMillis().appendSuffix("ms")
    .toFormatter()

fun Duration?.toPrettyString(): String =
    if (this != null) prettyFormatter.print(org.joda.time.Duration.millis(this.toMillis()).toPeriod()) else "infinite"

fun String.toDuration(): Duration =
    Duration.ofMillis(propertiesFormatter.parsePeriod(this).toStandardDuration().millis)