package pl.beone.lib.dockertestrunner.external

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.io.File

class TestContainerCoordinator(
    private val imageName: String,
    private val imageCustomEnabled: Boolean,
    private val imageCustomName: String,
    private val imageCustomDockerfilePath: String,
    private val imageCustomDockerfileFragmentPath: String,
    private val imageCustomDeleteOnExit: Boolean,
    private val projectPath: String,
    private val containerProjectPath: String,
    private val m2MountEnabled: Boolean,
    private val m2MountPath: String,
    private val containerM2MountPath: String,
    private val debuggerEnabled: Boolean,
    private val debuggerPort: Int
) {

    private lateinit var container: GenericContainer<*>

    fun init() {
        container = createContainer().apply {
            withFileSystemBind(projectPath, containerProjectPath)

            if (m2MountEnabled) {
                withFileSystemBind(m2MountPath, containerM2MountPath)
            }

            if (debuggerEnabled) {
                val debuggerPort = debuggerPort
                withExposedPorts(debuggerPort)
                withCreateContainerCmdModifier {
                    it.withPortBindings(
                        PortBinding(
                            Ports.Binding.bindPort(debuggerPort),
                            ExposedPort(debuggerPort)
                        )
                    )
                }
            }

            setEmptyWaitStrategy()

            withCommand("sleep", "infinity")
        }
    }

    private fun createContainer(): GenericContainer<Nothing> =
        if (imageCustomEnabled) {
            GenericContainer(
                ImageFromDockerfile(imageCustomName, imageCustomDeleteOnExit)
                    .withFileFromString("Dockerfile", createDockerfileWithReplacedTransformerPlaceholder())
            )
        } else {
            GenericContainer(imageName)
        }

    fun start() {
        verifyIfContainerWasInitialized()

        container.start()
    }

    fun stop() {
        try {
            verifyIfContainerWasInitialized()
        } catch (e: Exception) {
            return
        }

        container.stop()
    }

    fun execInContainer(vararg command: String): Container.ExecResult {
        verifyIfContainerWasInitialized()

        return container.execInContainer(*command)
    }

    private fun verifyIfContainerWasInitialized() {
        if (!::container.isInitialized) {
            throw RuntimeException("Container isn't initialized. Did you call init() method?")
        }
    }

    private fun createDockerfileWithReplacedTransformerPlaceholder(): String {
        val dockerfileContent = File(imageCustomDockerfilePath).readText()
        val dockerfileTransformerContent = File(imageCustomDockerfileFragmentPath).readText()

        return dockerfileContent.replace("\${DOCKERFILE-FRAGMENT}", dockerfileTransformerContent)
    }

    // at startup, Maven isn't run so you can't wait to start listening on given port
    // port is open when test is executed
    private fun GenericContainer<Nothing>.setEmptyWaitStrategy() {
        setWaitStrategy(object : AbstractWaitStrategy() {
            override fun waitUntilReady() {
            }
        })
    }
}