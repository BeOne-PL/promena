package pl.beone.lib.dockertestrunner.external

import org.junit.runners.model.FrameworkMethod
import org.testcontainers.containers.Container
import pl.beone.lib.dockertestrunner.applicationmodel.DockerTestException


class MavenOnTestContainerRunner(private val testContainerCoordinator: TestContainerCoordinator,
                                 private val debuggerEnabled: Boolean,
                                 private val debuggerPort: Int) {

    fun runTest(testClass: Class<*>, method: FrameworkMethod) {
        val mavenTestClassifier = createMavenTestClassifier(testClass, method)
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

    private fun createMavenTestClassifier(testClass: Class<*>, method: FrameworkMethod): String =
            "${testClass.name}#${method.name}"

    private fun createLogFilePath(mavenTestClassifier: String): String =
            "/tmp/$mavenTestClassifier.out"

    private fun runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier: String,
                                                                          logFilePath: String): Container.ExecResult {
        val command = listOfNotNull("mvn", "-f", "/test/pom.xml", "-Dtest=$mavenTestClassifier",
                                    determineDebuggerParameters(),
                                    "--log-file", logFilePath,
                                    "surefire:test")
                .joinToString(" ")

        return testContainerCoordinator.execInContainer("bash", "-c", command)
    }

    private fun determineDebuggerParameters(): String? {
        return if (debuggerEnabled) {
            """ -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:$debuggerPort -Xnoagent -Djava.compiler=NONE" """.trim()
        } else {
            null
        }
    }

    private fun readOutputFromContainerMavenLogFile(logFilePath: String): String {
        if (testContainerCoordinator.execInContainer("test", "-e", logFilePath).exitCode != 0) {
            throw DockerTestException("There is no Maven log file. Maybe Maven isn't installed in given image")
        }

        return testContainerCoordinator.execInContainer("cat", logFilePath)
                .stdout
    }
}