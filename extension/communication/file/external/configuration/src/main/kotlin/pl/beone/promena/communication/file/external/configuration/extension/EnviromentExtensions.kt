package pl.beone.promena.communication.file.external.configuration.extension

import org.springframework.core.env.Environment

internal fun Environment.getId(): String =
    getRequiredProperty("communication.file.external.id")