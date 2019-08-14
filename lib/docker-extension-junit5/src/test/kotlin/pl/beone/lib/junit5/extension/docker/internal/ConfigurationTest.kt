package pl.beone.lib.junit5.extension.docker.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

class ConfigurationTest {

    @Test
    fun getProperty() {
        val configuration = Configuration()

        configuration.getProperty("docker.test.image.custom.name") shouldBe "docker-extension-junit5:test"

        shouldThrow<NoSuchElementException> {
            configuration.getProperty("absent.property")
        }.message shouldBe "There is no <absent.property> element"
    }

    @Test
    fun `getProperty with casting`() {
        val configuration = Configuration()

        configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java) shouldBe true

        shouldThrow<NoSuchElementException> {
            configuration.getProperty("absent.property", Boolean::class.java)
        }.message shouldBe "There is no <absent.property> element"
    }

    @Test
    fun `getProperty with placeholder`() {
        val configuration = Configuration()

        configuration.getProperty("docker.test.maven.container.test.run-after") shouldBe "chown -R 1000:1000 /test"
    }

    @Test
    fun `getProperty _ docker-test_properties exist`() {
        mockDockerTestProperties {
            val configuration = Configuration()

            configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java) shouldBe true

            configuration.getProperty("docker.test.image.custom.name") shouldBe "docker-test-runner:changed-test"

            configuration.getProperty("additional.property") shouldBe "no matter"
        }
    }

    private fun mockDockerTestProperties(toRun: () -> Unit) {
        val file = File(getRootResource() + "docker-test.properties")
            .apply {
                writeText(
                    """
                        docker.test.image.custom.name=docker-test-runner:changed-test
                        additional.property=no matter
                    """.trimIndent()
                )
            }

        try {
            toRun()
        } finally {
            file.delete()
        }
    }

    private fun getRootResource(): String =
        Configuration::class.java.classLoader?.getResource(".")?.path ?: throw IOException("Couldn't get resource root path (.)")
}