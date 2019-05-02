package pl.beone.lib.dockertestrunner.external

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DockerTestRunner::class)
class DockerTestRunnerTest {

    @Test
    fun test() {
        assertThat(true).isEqualTo(true)
    }
}