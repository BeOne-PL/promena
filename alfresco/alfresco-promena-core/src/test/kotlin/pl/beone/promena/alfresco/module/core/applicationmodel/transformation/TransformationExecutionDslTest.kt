package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import org.junit.Test

class TransformationExecutionDslTest {

    @Test
    fun transformationExecution() {
        val id = "test"
        transformationExecution(id).id shouldBe id
    }
}