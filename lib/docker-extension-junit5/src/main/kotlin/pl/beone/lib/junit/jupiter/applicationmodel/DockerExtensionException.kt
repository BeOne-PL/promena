package pl.beone.lib.junit.jupiter.applicationmodel

open class DockerExtensionException(
    mavenLog: String
) : Exception(mavenLog)