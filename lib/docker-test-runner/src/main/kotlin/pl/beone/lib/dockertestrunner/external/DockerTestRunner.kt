package pl.beone.lib.dockertestrunner.external

import org.junit.Ignore
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import pl.beone.lib.dockertestrunner.internal.Configuration
import java.io.File

class DockerTestRunner(
    private val testClass: Class<*>
) : BlockJUnit4ClassRunner(testClass) {

    private val configuration = Configuration()

    private val testContainerCoordinator = TestContainerCoordinator(
        configuration.getProperty("docker.test.image.name"),
        configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java),
        configuration.getProperty("docker.test.image.custom.name"),
        configuration.getProperty("docker.test.image.custom.docker.directory.path"),
        configuration.getProperty("docker.test.image.custom.docker.dockerfile.name"),
        configuration.getProperty("docker.test.image.custom.docker-module.directory.path"),
        configuration.getProperty("docker.test.image.custom.docker-module.dockerfile-fragment.name"),
        configuration.getProperty("docker.test.image.custom.deleteOnExit", Boolean::class.java),
        configuration.getProperty("docker.test.project.path"),
        configuration.getProperty("docker.test.project.container.path"),
        configuration.getProperty("docker.test.m2.mount.enabled", Boolean::class.java),
        configuration.getProperty("docker.test.m2.mount.path"),
        configuration.getProperty("docker.test.m2.container.mount.path"),
        configuration.getProperty("docker.test.debugger.enabled", Boolean::class.java),
        configuration.getProperty("docker.test.debugger.port", Int::class.java)
    )


    private val mavenOnTestContainerRunner = MavenOnTestContainerRunner(
        testContainerCoordinator,
        configuration.getProperty("docker.test.maven.container.test.command"),
        configuration.getProperty("docker.test.maven.container.test.run-after"),
        configuration.getProperty("docker.test.project.container.path"),
        configuration.getProperty("docker.test.debugger.enabled", Boolean::class.java),
        configuration.getProperty("docker.test.debugger.port", Int::class.java)
    )

    override fun run(notifier: RunNotifier) {

        try {
            runOnHost {
                testContainerCoordinator.apply {
                    init()
                    start()
                }
            }

            super.run(notifier)
        } finally {
            runOnHost {
                testContainerCoordinator.stop()
            }
        }
    }

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        if (method.isSpecialKotlinName() || method.hasIgnoreAnnotation()) {
            notifier.fireTestIgnored(method)
        } else {
            runOnDocker {
                super.runChild(method, notifier)
            }

            runOnHost {
                val description = describeChild(method)

                try {
                    notifier.fireTestStarted(description)

                    mavenOnTestContainerRunner.runTest(testClass, method)
                } catch (e: Throwable) {
                    notifier.fireTestFailure(Failure(description, e))
                } finally {
                    notifier.fireTestFinished(description)
                }
            }
        }
    }

    private fun FrameworkMethod.isSpecialKotlinName(): Boolean =
        this.name.contains(" ")

    private fun FrameworkMethod.hasIgnoreAnnotation(): Boolean =
        this.getAnnotation(Ignore::class.java) != null

    private fun RunNotifier.fireTestIgnored(method: FrameworkMethod) {
        this.fireTestIgnored(Description.createTestDescription(testClass, method.name))
    }

    private fun onDocker(): Boolean =
        File("/.dockerenv").exists()

    private fun runOnHost(toRun: () -> Unit) {
        if (!onDocker()) {
            toRun()
        }
    }

    private fun runOnDocker(toRun: () -> Unit) {
        if (onDocker()) {
            toRun()
        }
    }
}