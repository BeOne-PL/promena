package pl.beone.promena.alfresco.module.client.http.configuration

import org.springframework.util.PropertyPlaceholderHelper
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