package pl.beone.promena.alfresco.module.client.http.configuration

import org.joda.time.format.PeriodFormatterBuilder
import org.slf4j.Logger
import org.springframework.util.PropertyPlaceholderHelper
import java.io.File
import java.net.URI
import java.time.Duration
import java.util.*

internal fun Properties.getRequiredPropertyWithResolvedPlaceholders(key: String): String {
    val property = getPropertyWithEmptySupport(key) ?: throw IllegalStateException("Required key '$key' not found")

    return PropertyPlaceholderHelper("\${", "}")
            .replacePlaceholders(property, this)

}

internal fun Properties.getPropertyWithEmptySupport(key: String): String? =
        getProperty(key).let {
            if (it == null || it.isEmpty()) {
                null
            } else {
                it
            }
        }

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