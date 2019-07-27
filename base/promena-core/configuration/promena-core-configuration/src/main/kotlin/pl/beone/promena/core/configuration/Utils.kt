package pl.beone.promena.core.configuration

import org.joda.time.format.PeriodFormatterBuilder
import java.time.Duration

internal fun String.toDuration(): Duration {
    val formatter = PeriodFormatterBuilder()
        .appendDays().appendSuffix("d ")
        .appendHours().appendSuffix("h ")
        .appendMinutes().appendSuffix("m")
        .appendSeconds().appendSuffix("s")
        .appendMillis().appendSuffix("ms")
        .toFormatter()

    return Duration.ofMillis(formatter.parsePeriod(this).toStandardDuration().millis)
}