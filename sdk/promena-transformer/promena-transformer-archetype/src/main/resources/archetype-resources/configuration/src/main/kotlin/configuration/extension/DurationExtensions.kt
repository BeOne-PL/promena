package ${package}.configuration.extension

import org.joda.time.format.PeriodFormatterBuilder
import java.time.Duration

fun String.toDuration(): Duration =
    Duration.ofMillis(
        PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .appendMillis().appendSuffix("ms")
            .toFormatter()
            .parsePeriod(this).toStandardDuration().millis
    )