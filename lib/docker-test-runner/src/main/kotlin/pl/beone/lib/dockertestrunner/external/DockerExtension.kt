package pl.beone.lib.dockertestrunner.external

import org.junit.jupiter.api.extension.*
import pl.beone.lib.dockertestrunner.internal.Configuration
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class DockerExtension : BeforeAllCallback, AfterAllCallback, InvocationInterceptor {

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

    override fun beforeAll(context: ExtensionContext) {
        runOnHost {
            testContainerCoordinator.apply {
                init()
                start()
            }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        runOnHost {
            testContainerCoordinator.stop()
        }
    }

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext
    ) {
        runOnHost {
            mavenOnTestContainerRunner.runTest(invocationContext.executable)
            invocation.proceed() // TODO mark as executed (ValidatingInvocation)
        }

        runOnDocker {
            super.interceptTestMethod(invocation, invocationContext, extensionContext)
        }
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