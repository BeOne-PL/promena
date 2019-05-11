package pl.beone.lib.dockertestrunner.external

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DockerTestRunner::class)
class DockerTestRunnerTest {

    @Test
    fun simpleTest() {
        assertThat(true).isEqualTo(true)
    }

    @Test
    fun checkIfDockerFragmentWasUsedToBuildImage() {
        String(Runtime.getRuntime().exec("cat /test.txt").inputStream.readAllBytes()).trim()
                .let { assertThat(it).isEqualTo("test") }
    }

    @Test
    fun `name with spaces _ should be ignored`() {
        assertThat(true).isEqualTo(false)
    }

    @Ignore
    @Test
    fun ignoreAnnotation_shouldBeIgnored() {
        assertThat(true).isEqualTo(false)
    }
}