package pl.beone.promena.alfresco.module.client.messagebroker.configuration

import org.slf4j.Logger
import org.springframework.util.PropertyPlaceholderHelper
import pl.beone.promena.alfresco.module.client.messagebroker.configuration.external.FileAlfrescoDataConverterContext
import java.io.File
import java.net.URI
import java.util.*

internal fun Properties.getPropertyWithResolvedPlaceholders(key: String): String? {
    val property = getProperty(key) ?: return null

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

internal fun Properties.getRequiredProperty(key: String): String =
    getProperty(key) ?: throw IllegalStateException("Required key '$key' not found")

internal fun Properties.getAndVerifyLocation(logger: Logger): URI? {
    val property = this.getPropertyWithEmptySupport("promena.communication.file.location")

    if (property == null) {
        logger.info("Property <promena.communication.file.location> is empty. Data will be sending to Promena using memory")
        return null
    }

    val uri = URI(property)

    try {
        logger.info("Property <promena.communication.file.location> isn't empty. Data will be sending to Promena using file")
        File(uri).exists()
    } catch (e: Exception) {
        throw IllegalArgumentException("Communication location <$uri> isn't correct", e)
    }

    return uri
}