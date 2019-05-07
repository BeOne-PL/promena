package pl.beone.lib.dockertestrunner.external

import org.assertj.core.api.Assertions.assertThat
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
        String(Runtime.getRuntime().exec("cat /tmp/test").inputStream.readAllBytes()).trim()
                .let { assertThat(it).isEqualTo("test") }
    }
}