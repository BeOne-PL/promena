package pl.beone.lib.dockertestrunner.external

import mu.KotlinLogging
import org.junit.runners.model.FrameworkMethod
import org.testcontainers.containers.Container
import pl.beone.lib.dockertestrunner.applicationmodel.DockerTestException

class MavenOnTestContainerRunner(
    private val testContainerCoordinator: TestContainerCoordinator,
    private val mavenContainerTestCommand: String,
    private val mavenContainerTestRunAfter: String,
    private val containerProjectPath: String,
    private val debuggerEnabled: Boolean,
    private val debuggerPort: Int
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun runTest(testClass: Class<*>, method: FrameworkMethod) {
        val mavenTestClassifier = createMavenTestClassifier(testClass, method)
        val logFilePath = createLogFilePath(mavenTestClassifier)

        try {
            val result = runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier, logFilePath)

            if (checkIfLogFileExists(logFilePath)) {
                val mavenLog = readOutputFromContainerMavenLogFile(logFilePath)
                if (result.exitCode != 0) {
                    throw DockerTestException(mavenLog)
                } else {
                    logger.error { mavenLog }
                }
            } else {
                throw DockerTestException(result.stderr)
            }
        } finally {
            testContainerCoordinator.execInContainer("bash", "-c", mavenContainerTestRunAfter)
        }
    }

    private fun createMavenTestClassifier(testClass: Class<*>, method: FrameworkMethod): String =
        "${testClass.name}#${method.name}"

    private fun createLogFilePath(mavenTestClassifier: String): String =
        "/tmp/$mavenTestClassifier.out"

    private fun runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier: String, logFilePath: String): Container.ExecResult {
        val command = listOfNotNull(
            "mvn", "-f", "$containerProjectPath/pom.xml", "-Dtest=$mavenTestClassifier",
            determineDebuggerParameters(),
            "--log-file", logFilePath,
            mavenContainerTestCommand
        )
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

    private fun checkIfLogFileExists(logFilePath: String): Boolean =
        testContainerCoordinator.execInContainer("test", "-e", logFilePath).exitCode == 0

    private fun readOutputFromContainerMavenLogFile(logFilePath: String): String =
        testContainerCoordinator.execInContainer("cat", logFilePath)
            .stdout
}