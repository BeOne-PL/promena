package pl.beone.promena.alfresco.module.client.messagebroker.configuration

import org.joda.time.format.PeriodFormatterBuilder
import org.slf4j.Logger
import org.springframework.util.PropertyPlaceholderHelper
import java.io.File
import java.net.URI
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

internal fun Properties.getAndVerifyLocation(logger: Logger): URI? {
    val property = this.getPropertyWithEmptySupport("promena.client.communication.file.location")

    if (property == null) {
        logger.info("Property <promena.client.communication.file.location> is empty. Data will be sending to Promena using memory")
        return null
    }

    val uri = URI(property)

    try {
        logger.info("Property <promena.client.communication.file.location> isn't empty. Data will be sending to Promena using file")
        File(uri).exists()
    } catch (e: Exception) {
        throw IllegalArgumentException("Communication location <$uri> isn't correct", e)
    }

    return uri
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