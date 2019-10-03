package pl.beone.lib.junit.jupiter.external

import org.junit.jupiter.api.extension.*
import pl.beone.lib.junit.jupiter.applicationmodel.DockerExtensionException
import pl.beone.lib.junit.jupiter.internal.Configuration
import java.io.File
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean

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
        if (context.getParents().doesNotContainJupiterEngineExecutionContext()) {
            throw DockerExtensionException("DockerExtension supports only JupiterTestEngine")
        }

        runOnHost {
            testContainerCoordinator.apply {
                init()
                start()
            }
        }
    }

    private fun ExtensionContext.getParents(): List<ExtensionContext> =
        if (parent.isPresent) {
            val parent = parent.get()
            parent.getParents() + parent
        } else {
            emptyList()
        }

    private fun List<ExtensionContext>.doesNotContainJupiterEngineExecutionContext(): Boolean =
        !any { it.javaClass.canonicalName == "org.junit.jupiter.engine.descriptor.JupiterEngineExtensionContext" }


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
        val method = invocationContext.executable
        if (method.isSpecialKotlinName()) {
            throw DockerExtensionException("DockerExtension supports only classic Java test names (without spaces)")
        }

        runOnHost {
            mavenOnTestContainerRunner.runTest(method)
            markTestAsExecuted(invocation)
        }

        runOnDocker {
            super.interceptTestMethod(invocation, invocationContext, extensionContext)
        }
    }

    private fun Method.isSpecialKotlinName(): Boolean =
        this.name.contains(" ")

    private fun markTestAsExecuted(invocation: InvocationInterceptor.Invocation<Void>) {
        Class.forName("org.junit.jupiter.engine.execution.InvocationInterceptorChain\$ValidatingInvocation")
            .getDeclaredField("invoked")
            .also { field -> field.isAccessible = true }
            .also { field -> (field.get(invocation) as AtomicBoolean).set(true) }
            .also { field -> field.isAccessible = false }
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