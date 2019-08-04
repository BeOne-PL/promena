package pl.beone.lib.dockertestrunner.external

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith

@ExtendWith(DockerExtension::class)
class DockerExtensionTest {

    @Test
    fun simpleTest() {
        true shouldBe true
    }

//    @Test
//    fun checkIfDockerFragmentWasUsedToBuildImage() {
//        String(readTestFileUsingCat()).trim()
//            .let { it shouldBe "test" }
//    }
//
//    @Test
//    fun `name with spaces _ should be ignored`() {
//        true shouldBe false
//    }

    @Test
//    @Disabled
    fun ignoreAnnotation_shouldBeIgnored() {
        true shouldBe false
    }

    private fun readTestFileUsingCat(): ByteArray =
        Runtime.getRuntime().exec("cat /test.txt").inputStream.readAllBytes()
}

@ExtendWith(DockerExtension::class)
class MyTests : FunSpec({
    test("String length should return the length of the string") {
        "sammy".length shouldBe 6
        "".length shouldBe 0
    }
})