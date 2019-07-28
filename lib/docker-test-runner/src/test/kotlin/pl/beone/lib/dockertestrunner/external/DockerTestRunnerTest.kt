package pl.beone.lib.dockertestrunner.external

import io.kotlintest.shouldBe
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(DockerTestRunner::class)
class DockerTestRunnerTest {

    @Test
    fun simpleTest() {
        true shouldBe true
    }

    @Test
    fun checkIfDockerFragmentWasUsedToBuildImage() {
        String(readTestFileUsingCat()).trim()
            .let { it shouldBe "test" }
    }

    @Test
    fun `name with spaces _ should be ignored`() {
        true shouldBe false
    }

    @Ignore
    @Test
    fun ignoreAnnotation_shouldBeIgnored() {
        true shouldBe false
    }

    private fun readTestFileUsingCat(): ByteArray =
        Runtime.getRuntime().exec("cat /test.txt").inputStream.readAllBytes()
}