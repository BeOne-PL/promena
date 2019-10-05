package ${package}.configuration.extension

import org.springframework.core.env.Environment

fun Environment.getNotBlankProperty(key: String): String? {
    val value = getRequiredProperty(key)
    return if (value.isNotBlank()) {
        value
    } else {
        null
    }
}

fun Environment.getRequiredNotBlankProperty(key: String): String =
    getRequiredProperty(key)
        .also { check(!it.isBlank()) { "Required key '$key' is blank" } }