package pl.beone.promena.maven.plugin.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.descriptor.PluginDescriptor
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.net.URL
import java.net.URLClassLoader

@Mojo(name = "build")
class BuildMojo : AbstractMojo() {

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

    @Parameter(property = "appJar", defaultValue = "\${project.artifactId}-\${project.version}.jar", required = true)
    private lateinit var appJar: String

    @Parameter(property = "name", required = true)
    private lateinit var name: String

    @Parameter(property = "version", required = true)
    private lateinit var version: String

    override fun execute() {
        val dockerClient = createDockerClient()

        val transformerDockerfileFragments = getTransformerDockerfileFragments()

        log.info("Building docker image: ${getImageFullName()}...")
        if (log.isDebugEnabled) {
            log.debug(" Jar: ${File(outputDirectory, appJar).path}")
            log.debug(" Dockerfile: ${File(context, dockerfile).path}")
            log.debug(" Replacing <\${DOCKERFILE-FRAGMENT}> in Dockerfile using <$dockerfileFragment> files from transformers")
        }

        onProcessedDockerfile(transformerDockerfileFragments) { dockerfile ->
            dockerClient.buildImageCmd(dockerfile)
                .withTags(setOf(getImageFullName()))
                .exec(LoggerBuildImageResultCallback(log))
                .awaitImageId()
        }

        log.info("\n")
        log.info("Finished building docker image: ${getImageFullName()}")
    }

    private fun getImageFullName(): String =
        "$name:$version"

    private fun createDockerClient(): DockerClient =
        DockerClientBuilder
            .getInstance(
                DefaultDockerClientConfig.createDefaultConfigBuilder()
            )
            .build()

    private fun getTransformerDockerfileFragments(): List<String> =
        pluginDescriptor.artifacts.map { it.getDescription() to it.getResourceUrl(dockerfileFragment) }
            .filter { (_, resourceUrl) -> resourceUrl != null }
            .also {
                log.info("Found $dockerfileFragment in <${it.size}> transformers:")
                it.forEach { (artifactDescription, _) -> log.info("> $artifactDescription") }
            }
            .map { (artifactDescription, resourceUrl) -> "# $artifactDescription" + "\n" + resourceUrl!!.readText() }

    private fun Artifact.getDescription(): String =
        this.groupId + ":" + this.artifactId + ":" + this.version

    private fun Artifact.getResourceUrl(resourcePath: String): URL? =
        URLClassLoader(arrayOf(file.toURI().toURL()))
            .getResource(resourcePath)

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

        log.debug("Copying jar from <$appJarFile> to <$processedAppJarFile>...")
        appJarFile.copyTo(processedAppJarFile, true)
        log.debug("Finished copying jar from <$appJarFile> to <$processedAppJarFile>")

        return processedAppJarFile
    }

    private fun processDockerfile(transformerDockerfile: String): File {
        val dockerfileFile = File(context, dockerfile)
        val processedDockerfileFile = File(context, "Dockerfile-processed")

        val processedDockerfileContent =
            dockerfileFile
                .readText()
                .replace("\${DOCKERFILE-FRAGMENT}", transformerDockerfile)
                .replace("\${APP_JAR}", appJar)
        log.debug("Generated Dockerfile:" + "\n" + processedDockerfileContent)

        return processedDockerfileFile.apply {
            writeText(processedDockerfileContent)
        }
    }
}
