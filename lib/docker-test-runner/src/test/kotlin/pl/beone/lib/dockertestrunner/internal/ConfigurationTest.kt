package pl.beone.lib.dockertestrunner.internal

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import java.io.File

class ConfigurationTest {

    @Test
    fun getProperty() {
        val configuration = Configuration()

        configuration.getProperty("docker.test.image.custom.name") shouldBe "docker-test-runner:test"

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
    fun `getProperty _ docker-test_properties exist`() {
        mockDockerTestProperties {
            val configuration = Configuration()

            configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java) shouldBe true

            configuration.getProperty("docker.test.image.custom.name") shouldBe "docker-test-runner:changed-test"

            configuration.getProperty("additional.property") shouldBe "no matter"
        }
    }

    private fun mockDockerTestProperties(toRun: () -> Unit) {
        val file = File(Configuration::class.java.classLoader.getResource(".").path + "docker-test.properties")
                .apply {
                    writeText("""
                        docker.test.image.custom.name=docker-test-runner:changed-test
                        additional.property=no matter
                    """.trimIndent())
                }

        try {
            toRun()
        } finally {
            file.delete()
        }
    }
}