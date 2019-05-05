package pl.beone.lib.dockertestrunner.internal

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.io.File

class ConfigurationTest {

    @Test
    fun getProperty() {
        val configuration = Configuration()

        assertThat(configuration.getProperty("docker.test.image.custom.name"))
                .isEqualTo("docker-test-runner:test")

        assertThatThrownBy { configuration.getProperty("absent.property") }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent.property> element")
    }

    @Test
    fun `getProperty with casting`() {
        val configuration = Configuration()

        assertThat(configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java))
                .isEqualTo(true)

        assertThatThrownBy { configuration.getProperty("absent.property", Boolean::class.java) }
                .isExactlyInstanceOf(NoSuchElementException::class.java)
                .hasMessage("There is no <absent.property> element")
    }

    @Test
    fun `getProperty _ docker-test_properties exist`() {
        mockDockerTestProperties {
            val configuration = Configuration()

            assertThat(configuration.getProperty("docker.test.image.custom.enabled", Boolean::class.java))
                    .isEqualTo(true)

            assertThat(configuration.getProperty("docker.test.image.custom.name"))
                    .isEqualTo("docker-test-runner:changed-test")

            assertThat(configuration.getProperty("additional.property"))
                    .isEqualTo("no matter")
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