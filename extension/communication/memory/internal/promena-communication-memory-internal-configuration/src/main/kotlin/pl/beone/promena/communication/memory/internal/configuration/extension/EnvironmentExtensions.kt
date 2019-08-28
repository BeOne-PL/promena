package pl.beone.promena.communication.memory.internal.configuration.extension

import org.springframework.core.env.Environment

fun Environment.getId(): String =
    getRequiredProperty("communication.memory.internal.id")