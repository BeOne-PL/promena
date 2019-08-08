package pl.beone.promena.communication.memory.external.configuration.internal

import org.springframework.core.env.Environment

fun Environment.getId(): String =
    getRequiredProperty("communication.memory.external.id")