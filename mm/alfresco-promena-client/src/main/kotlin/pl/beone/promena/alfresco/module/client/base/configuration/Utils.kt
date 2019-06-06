package pl.beone.promena.alfresco.module.client.base.configuration

import org.slf4j.Logger
import java.io.File
import java.net.URI
import java.util.*


internal fun Properties.getRequiredProperty(key: String): String =
        getPropertyWithEmptySupport(key) ?: throw IllegalStateException("Required key '$key' not found")

internal fun Properties.getPropertyWithEmptySupport(key: String): String? =
        getProperty(key).let {
            if (it == null || it.isEmpty()) {
                null
            } else {
                it
            }
        }

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