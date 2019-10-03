package pl.beone.promena.maven.plugin.docker

import java.nio.file.Path

internal data class ArtifactDescriptor(
    val description: String,
    val dockerFragment: String,
    val paths: List<Path>
)