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
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

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

    @Parameter(property = "dockerResourcePath", defaultValue = "docker", required = true)
    private lateinit var dockerResourcePath: String

    @Parameter(property = "dockerfileFragment", defaultValue = "Dockerfile-fragment", required = true)
    private lateinit var dockerfileFragment: String

    @Parameter(property = "appJar", defaultValue = "\${project.artifactId}-\${project.version}.jar", required = true)
    private lateinit var appJar: String

    @Parameter(property = "name", required = true)
    private lateinit var name: String

    @Parameter(property = "version", required = true)
    private lateinit var version: String

    override fun execute() {
        val transformerArtifactDescriptor = getTransformerArtifactDescriptors()

        if (log.isDebugEnabled) {
            log.debug(" Jar: ${File(outputDirectory, appJar).path}")
            log.debug(" Dockerfile: ${File(context, dockerfile).path}")
            log.debug(" Replacing <\${DOCKERFILE-FRAGMENT}> in Dockerfile using <$dockerfileFragment> files from transformer artifacts")
        }

        val tmpDirectory = createTempDir()
        try {
            log.info("Docker context directory: ${tmpDirectory.path}")

            log.info("Copying application jar and Docker files...")
            tmpDirectory
                .also(::copyDockerDirectory)
                .also(::copyApplicationJar)
                .also { copyArtifactsPaths(transformerArtifactDescriptor, it) }
            log.info("Finished copying application jar and Docker files")

            log.info("Processing Dockerfile...")
            val processedDockerfileFile = concatDockerfileFragments(transformerArtifactDescriptor)
                .let(::replacePlaceholdersInDockerfile)
                .let { processedDockerfile -> saveDockerfile(tmpDirectory, processedDockerfile) }
            log.info("Finished processing Dockerfile")

            log.info("Building docker image: ${getImageFullName()}...")
            buildImage(processedDockerfileFile)
            log.info("Finished building docker image: ${getImageFullName()}")
        } finally {
            tmpDirectory.deleteRecursively()
        }
    }

    private fun getTransformerArtifactDescriptors(): List<ArtifactDescriptor> =
        pluginDescriptor.artifacts
            .filter(::containsDockerfileFragment)
            .map { ArtifactDescriptor(generateDescription(it), readDockerfileFragment(it), getDockerPaths(it)) }
            .also { artifactDescriptors ->
                log.info("Found $dockerfileFragment in <${artifactDescriptors.size}> transformers:")
                artifactDescriptors.forEach { (artifactDescription) -> log.info("> $artifactDescription") }
            }

    private fun containsDockerfileFragment(artifact: Artifact): Boolean =
        getDockerfileFragmentUri(artifact) != null

    private fun generateDescription(artifact: Artifact): String =
        "${artifact.groupId}:${artifact.artifactId}:${artifact.version}"

    private fun readDockerfileFragment(artifact: Artifact): String =
        getDockerfileFragmentUri(artifact)!!.readText()

    private fun getDockerPaths(artifact: Artifact): List<Path> =
        FileSystems.newFileSystem(URI("jar:" + artifact.file.toURI()), emptyMap<String, Any>())
            .getPath(dockerResourcePath)
            .let { dockerResourceAbsolutePath -> Files.walk(dockerResourceAbsolutePath).toList() }

    private fun getImageFullName(): String =
        "$name:$version"

    private fun getDockerfileFragmentUri(artifact: Artifact): URL? =
        URLClassLoader(arrayOf(artifact.file.toURI().toURL()))
            .getResource("$dockerResourcePath/$dockerfileFragment")

    private fun copyDockerDirectory(tmpDirectory: File) {
        context.copyRecursively(tmpDirectory)
    }

    private fun copyApplicationJar(destinationDirectory: File) {
        val applicationJarFile = File(outputDirectory, appJar)
        val destinationApplicationJarFile = File(destinationDirectory, appJar)

        log.debug("Copying application jar from <$applicationJarFile> to <$destinationApplicationJarFile>...")
        applicationJarFile.copyTo(destinationApplicationJarFile)
        log.debug("Finished copying application jar from <$applicationJarFile> to <$destinationApplicationJarFile>")
    }

    private fun copyArtifactsPaths(artifactDescriptors: List<ArtifactDescriptor>, destinationDirectory: File) {
        artifactDescriptors.flatMap { it.paths }
            .forEach {
                val artifactsAbsolutePath = it.fileSystem.toString() + it
                val artifactsRelativePath = it.toRealPath().toString().removePrefix("/$dockerResourcePath/")

                val destinationFile = File(destinationDirectory, artifactsRelativePath)
                val destinationPath = destinationFile.path

                if (Files.isDirectory(it)) {
                    log.debug("Creating directory: $destinationPath...")
                    destinationFile.mkdir()
                    log.debug("Finished creating directory: $destinationPath")
                } else {
                    if (isNotDockerFileFragment(artifactsRelativePath)) {
                        log.debug("Copying file from <$artifactsAbsolutePath> to <$destinationPath>...")
                        Files.newInputStream(it).copyTo(destinationFile.outputStream())
                        log.debug("Finished copying file from <$artifactsAbsolutePath> to <$destinationPath>")
                    } else {
                        log.debug("Skipped $artifactsAbsolutePath file")
                    }
                }
            }
    }

    private fun isNotDockerFileFragment(artifactsRelativePath: String): Boolean =
        artifactsRelativePath != dockerfileFragment

    private fun concatDockerfileFragments(artifactDescriptors: List<ArtifactDescriptor>): String =
        artifactDescriptors.joinToString("\n\n")
        { (description, dockerfileFragment) -> "# $description\n$dockerfileFragment" }

    private fun replacePlaceholdersInDockerfile(dockerfileFragments: String): String {
        return File(context, dockerfile)
            .readText()
            .replace("\${DOCKERFILE-FRAGMENT}", dockerfileFragments)
            .replace("\${APP_JAR}", appJar)
            .also { processedDockerfile ->
                if (log.isDebugEnabled) {
                    log.debug("Dockerfile:\n$processedDockerfile")
                }
            }
    }

    private fun saveDockerfile(destinationDirectory: File, readyDockerfile: String): File =
        File(destinationDirectory, "Dockerfile").apply {
            writeText(readyDockerfile)
        }

    private fun createDockerClient(): DockerClient =
        DockerClientBuilder
            .getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder())
            .build()

    private fun buildImage(processedDockerfileFile: File) {
        createDockerClient().buildImageCmd(processedDockerfileFile)
            .withTags(setOf(getImageFullName()))
            .exec(LoggerBuildImageResultCallback(log))
            .awaitImageId()
    }
}
