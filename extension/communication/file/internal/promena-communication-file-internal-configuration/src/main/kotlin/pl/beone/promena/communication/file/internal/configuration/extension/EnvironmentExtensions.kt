package pl.beone.promena.communication.file.internal.configuration.extension

import org.springframework.core.env.Environment
import java.net.URI

fun Environment.getId(): String =
    getRequiredProperty("communication.file.internal.id")

fun Environment.getLocation(): URI =
    URI(getRequiredProperty("communication.file.internal.location"))