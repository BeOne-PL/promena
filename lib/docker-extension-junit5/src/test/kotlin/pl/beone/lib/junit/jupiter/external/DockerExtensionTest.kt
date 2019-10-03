package pl.beone.lib.junit.jupiter.external

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DockerExtension::class)
class DockerExtensionTest {

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
    @Disabled
    fun ignoreAnnotation_shouldBeIgnored() {
        true shouldBe false
    }

//    Not testable because it happens behind this scope (before)
//    @Test
//    fun `name with spaces _ should throw DockerExtensionException because there is no support for special Kotlin names `() {
//        true shouldBe false
//    }

    private fun readTestFileUsingCat(): ByteArray =
        Runtime.getRuntime().exec("cat /test.txt").inputStream.readAllBytes()
}