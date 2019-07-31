package pl.beone.promena.alfresco.module.client.activemq.configuration

import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.util.PropertyPlaceholderHelper
import java.time.Duration
import java.util.*


internal fun Properties.getPropertyWithResolvedPlaceholders(key: String): String? {
    val property = getPropertyWithEmptySupport(key) ?: return null

    return PropertyPlaceholderHelper("\${", "}")
        .replacePlaceholders(property, this)
}

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