package pl.beone.promena.communication.file.internal.configuration.extension

import org.springframework.core.env.Environment
import java.io.File

fun Environment.getId(): String =
    getRequiredProperty("communication.file.internal.id")

fun Environment.getDirectory(): File =
    File(getRequiredProperty("communication.file.internal.directory.path"))
        .also { validate(it) }

private fun validate(directory: File) {
    require(directory.exists() && directory.isDirectory) { "Directory <$directory> doesn't exist or isn't a directory" }
}
