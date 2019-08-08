package pl.beone.promena.communication.memory.internal.configuration.internal

import org.springframework.core.env.Environment

fun Environment.getId(): String =
    getRequiredProperty("communication.memory.internal.id")