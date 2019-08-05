package pl.beone.lib.dockertestrunner.applicationmodel

class DockerExtensionTestExecutionException(
    mavenLog: String
) : DockerExtensionException("Test failed. Check the following log for more details:\n$mavenLog")