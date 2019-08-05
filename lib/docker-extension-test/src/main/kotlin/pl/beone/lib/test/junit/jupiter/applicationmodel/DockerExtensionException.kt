package pl.beone.lib.test.junit.jupiter.applicationmodel

open class DockerExtensionException(
    mavenLog: String
) : Exception(mavenLog)