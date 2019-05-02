package pl.beone.lib.dockertestrunner.applicationmodel

class DockerTestException(mavenLog: String)
    : Exception("Test failed. Check the following log for more details:\n$mavenLog")