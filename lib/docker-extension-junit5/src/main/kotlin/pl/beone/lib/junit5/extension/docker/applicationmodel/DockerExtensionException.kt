package pl.beone.lib.junit5.extension.docker.applicationmodel

open class DockerExtensionException(
    mavenLog: String
) : Exception(mavenLog)