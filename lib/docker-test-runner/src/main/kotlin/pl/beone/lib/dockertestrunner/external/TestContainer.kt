package pl.beone.lib.dockertestrunner.external

import org.junit.runners.model.FrameworkMethod
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import pl.beone.lib.dockertestrunner.applicationmodel.DockerTestException

class TestContainer(private val className: String) {

    private val container = createContainer()

    private fun createContainer(): GenericContainer<*> =
            GenericContainer<Nothing>("maven:slim").apply {
                withFileSystemBind(System.getProperty("user.dir"), "/test")
                withFileSystemBind("/Users/skotar/.m2", "/root/.m2")
                withCommand("sleep", "infinity")
            }

    fun start() {
        container.start()
    }

    fun stop() {
        container.stop()
    }

    fun runTest(method: FrameworkMethod) {
        val mavenTestClassifier = createMavenTestClassifier(method)
        val logFilePath = createLogFilePath(mavenTestClassifier)

        val result =
                runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier, logFilePath)

        val mavenLog = readOutputFromContainerMavenLogFile(logFilePath)

        if (result.exitCode != 0) {
            throw DockerTestException(mavenLog)
        } else {
            println(mavenLog)
        }
    }

    private fun createMavenTestClassifier(method: FrameworkMethod): String =
            "$className#${method.name}"

    private fun createLogFilePath(mavenTestClassifier: String): String =
            "/tmp/$mavenTestClassifier.out"

    private fun runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier: String,
                                                                          logFilePath: String): Container.ExecResult =
            container.execInContainer("mvn", "-f", "/test/pom.xml", "-Dtest=$mavenTestClassifier",
                                      "--log-file", logFilePath,
                                      "test")

    private fun readOutputFromContainerMavenLogFile(logFilePath: String): String {
        if (container.execInContainer("test", "-e", logFilePath).exitCode != 0) {
            throw DockerTestException("There is no Maven log file. Maybe Maven isn't installed in given image")
        }

        return container.execInContainer("cat", logFilePath)
                .stdout
    }
}