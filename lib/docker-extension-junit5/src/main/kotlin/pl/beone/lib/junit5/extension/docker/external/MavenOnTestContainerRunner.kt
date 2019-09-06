package pl.beone.lib.junit5.extension.docker.external

import mu.KotlinLogging
import org.testcontainers.containers.Container
import pl.beone.lib.junit5.extension.docker.applicationmodel.DockerExtensionTestExecutionException
import java.lang.reflect.Method

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

    fun runTest(method: Method) {
        val mavenTestClassifier = createMavenTestClassifier(method)
        val logFilePath = createLogFilePath(mavenTestClassifier)

        try {
            val result = runContainerWithMavenForGivenMethodAndSaveOutputInLogFile(mavenTestClassifier, logFilePath)

            if (checkIfLogFileExists(logFilePath)) {
                val mavenLog = readOutputFromContainerMavenLogFile(logFilePath)
                if (result.exitCode != 0) {
                    throw DockerExtensionTestExecutionException(mavenLog)
                } else {
                    logger.info { mavenLog }
                }
            } else {
                throw DockerExtensionTestExecutionException(result.stderr)
            }
        } finally {
            if (mavenContainerTestRunAfter.isNotBlank()) {
                testContainerCoordinator.execInContainer("bash", "-c", mavenContainerTestRunAfter)
            }
        }
    }

    private fun createMavenTestClassifier(method: Method): String =
        "${method.declaringClass.name}#${method.name}"

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

    private fun determineDebuggerParameters(): String? =
        if (debuggerEnabled) {
            """ -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:$debuggerPort -Xnoagent -Djava.compiler=NONE" """.trim()
        } else {
            null
        }

    private fun checkIfLogFileExists(logFilePath: String): Boolean =
        testContainerCoordinator.execInContainer("test", "-e", logFilePath).exitCode == 0

    private fun readOutputFromContainerMavenLogFile(logFilePath: String): String =
        testContainerCoordinator.execInContainer("cat", logFilePath)
            .stdout
}