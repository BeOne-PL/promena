package pl.beone.lib.junit5.extension.docker.applicationmodel

class DockerExtensionTestExecutionException(
    mavenLog: String
) : DockerExtensionException("Test failed. Check the following log for more details:\n$mavenLog")