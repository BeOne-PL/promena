package pl.beone.promena.maven.plugin.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.command.BuildImageResultCallback
import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader

@Mojo(name = "build")
class BuildMojo : AbstractMojo() {

    companion object {
        private val logger = LoggerFactory.getLogger(BuildMojo::class.java)
    }

    @Parameter(defaultValue = "\${plugin}")
    private lateinit var pluginDescriptor: PluginDescriptor

    @Parameter(property = "outputDir", defaultValue = "\${project.build.directory}", required = true)
    private lateinit var outputDirectory: File

    @Parameter(property = "context", defaultValue = "\${project.basedir}/src/docker", required = true)
    private lateinit var context: File

    @Parameter(property = "dockerfile", defaultValue = "Dockerfile", required = true)
    private lateinit var dockerfile: String

    @Parameter(property = "dockerfileFragment", defaultValue = "Dockerfile-fragment", required = true)
    private lateinit var dockerfileFragment: String

    @Parameter(property = "appJar",
               defaultValue = "\${project.artifactId}-\${project.version}.jar",
               required = true)
    private lateinit var appJar: String

    @Parameter(property = "name", required = true)
    private lateinit var name: String

    @Parameter(property = "version", required = true)
    private lateinit var version: String

    override fun execute() {
        val dockerClient = createDockerClient()

        val transformerDockerfileFragments = getTransformerDockerfileFragments()

        log.info("Building docker image <${getImageFullName()}> using <$dockerfile, $dockerfileFragment, $appJar> from <$context>")

        onProcessedDockerfile(transformerDockerfileFragments) {
            dockerClient.buildImageCmd(it)
                    .withTags(setOf(getImageFullName()))
                    .exec(BuildImageResultCallback())
                    .awaitImageId()
        }
    }

    private fun getImageFullName(): String =
            "$name:$version"

    private fun createDockerClient(): DockerClient {
        val config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
        return DockerClientBuilder
                .getInstance(config)
                .build()
    }

    private fun getTransformerDockerfileFragments(): List<String> =
            pluginDescriptor.artifacts.mapNotNull {
                val dockerfileFragment = URLClassLoader(arrayOf(it.file.toURI().toURL()))
                        .getResource(dockerfileFragment)?.readText()

                if (dockerfileFragment != null) {
                    logger.info("Found Dockerfile fragment in <${it.getDescription()}>")
                    "# ${it.getDescription()}\n$dockerfileFragment"
                } else {
                    null
                }
            }

    private fun Artifact.getDescription(): String =
            this.groupId + ":" + this.artifactId + ":" + this.version

    private fun onProcessedDockerfile(transformerDockerfileFragments: List<String>, toRun: (dockerfile: File) -> Unit) {
        val transformerDockerfile = transformerDockerfileFragments.joinToString("\n\n")
        val processedAppJarFile = copyJar()
        val processedDockerfileFile = processDockerfile(transformerDockerfile)

        try {
            toRun(processedDockerfileFile)
        } finally {
            processedDockerfileFile.delete()
            processedAppJarFile.delete()
        }
    }

    private fun copyJar(): File {
        val appJarFile = File(outputDirectory, appJar)
        val processedAppJarFile = File(context, appJar)
        logger.debug("Copying jar from <$appJarFile> to <$processedAppJarFile>")

        appJarFile.copyTo(processedAppJarFile, true)
        return processedAppJarFile
    }

    private fun processDockerfile(transformerDockerfile: String): File {
        val dockerfileFile = File(context, dockerfile)
        val processedDockerfileFile = File(context, "Dockerfile-processed")
        logger.debug("Writing to processed Dockerfile <$processedDockerfileFile> based on Dockerfile <$dockerfileFile>")

        val processedDockerfileContent =
                dockerfileFile
                        .readText()
                        .replace("\${DOCKERFILE-FRAGMENT}", transformerDockerfile)
                        .replace("\${APP_JAR}", appJar)
        logger.debug("Processed Dockerfile:\n$processedDockerfileContent")

        processedDockerfileFile.writeText(processedDockerfileContent)
        return processedDockerfileFile
    }
}
