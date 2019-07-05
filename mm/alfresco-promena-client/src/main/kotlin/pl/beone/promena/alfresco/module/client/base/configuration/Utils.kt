package pl.beone.promena.alfresco.module.client.base.configuration

import org.springframework.util.PropertyPlaceholderHelper
import java.io.File
import java.net.URI
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

internal fun Properties.getLocation(): URI {
    val property = this.getRequiredPropertyWithResolvedPlaceholders("promena.client.communication.file.location")

    val uri = URI(property)

    try {
        File(uri).exists()
    } catch (e: Exception) {
        throw IllegalArgumentException("Communication location <$uri> isn't correct", e)
    }

    return uri
}