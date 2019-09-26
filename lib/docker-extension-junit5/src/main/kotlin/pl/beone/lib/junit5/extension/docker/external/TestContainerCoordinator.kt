package pl.beone.lib.junit5.extension.docker.external

import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy
import org.testcontainers.images.builder.ImageFromDockerfile
import java.io.File
import kotlin.concurrent.thread

class TestContainerCoordinator(
    private val imageName: String,
    private val imageCustomEnabled: Boolean,
    private val imageCustomName: String,
    private val imageCustomDockerDirectoryPath: String,
    private val imageCustomDockerDockerfileName: String,
    private val imageCustomDockerModuleDirectoryPath: String,
    private val imageCustomDockerModuleDockerfileFragmentName: String,
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
            val tmpDirectory = createTempDirAndSetDeletionOnShutdown()

            copyDockerDirectories(tmpDirectory)
            replacePlaceholderInDockerfileByDockerfileFragment(tmpDirectory)

            GenericContainer(
                ImageFromDockerfile(imageCustomName, imageCustomDeleteOnExit)
                    .withFileFromPath(".", tmpDirectory.toPath())
            )
        } else {
            GenericContainer(imageName)
        }

    private fun createTempDirAndSetDeletionOnShutdown(): File =
        createTempDir().apply {
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                deleteRecursively()
            })
        }

    private fun replacePlaceholderInDockerfileByDockerfileFragment(tmpDirectory: File) {
        val dockerfileFragment = File(tmpDirectory, imageCustomDockerModuleDockerfileFragmentName)

        val dockerfileFragmentContent = if (dockerfileFragment.exists()) dockerfileFragment.readText() else ""

        val dockerfileFile = File(tmpDirectory, imageCustomDockerDockerfileName)
        val dockerfile = dockerfileFile.readText()

        dockerfileFile.writeText(
            dockerfile.replace("\${DOCKERFILE-FRAGMENT}", dockerfileFragmentContent)
        )
    }

    private fun copyDockerDirectories(tmpDirectory: File) {
        File(imageCustomDockerDirectoryPath).copyRecursively(tmpDirectory)
        File(imageCustomDockerModuleDirectoryPath).copyRecursively(tmpDirectory)
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

    private fun GenericContainer<Nothing>.setEmptyWaitStrategy() {
        setWaitStrategy(object : AbstractWaitStrategy() {
            override fun waitUntilReady() {
                // at startup, Maven isn't run so you can't wait to start listening on given port
                // port is open when test is executed
            }
        })
    }
}