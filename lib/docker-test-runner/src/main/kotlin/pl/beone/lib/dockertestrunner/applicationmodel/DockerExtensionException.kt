package pl.beone.lib.dockertestrunner.applicationmodel

open class DockerExtensionException(
    mavenLog: String
) : Exception(mavenLog)