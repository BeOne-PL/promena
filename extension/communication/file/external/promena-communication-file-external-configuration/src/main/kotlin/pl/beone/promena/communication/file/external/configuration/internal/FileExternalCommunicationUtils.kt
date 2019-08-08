package pl.beone.promena.communication.file.external.configuration.internal

import org.springframework.core.env.Environment

fun Environment.getId(): String =
    getRequiredProperty("communication.file.external.id")