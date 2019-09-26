package pl.beone.promena.alfresco.module.core.extension

import org.springframework.util.PropertyPlaceholderHelper
import java.util.*

fun Properties.getPropertyWithResolvedPlaceholders(key: String): String? {
    val property = getPropertyWithEmptySupport(key) ?: return null

    return PropertyPlaceholderHelper("\${", "}")
        .replacePlaceholders(property, this)
}

fun Properties.getRequiredPropertyWithResolvedPlaceholders(key: String): String {
    val property = getPropertyWithEmptySupport(key) ?: throw IllegalStateException("Required key '$key' not found")

    return PropertyPlaceholderHelper("\${", "}")
        .replacePlaceholders(property, this)

}

fun Properties.getPropertyWithEmptySupport(key: String): String? =
    getProperty(key).let {
        if (it != null && it.isNotEmpty()) it else null
    }