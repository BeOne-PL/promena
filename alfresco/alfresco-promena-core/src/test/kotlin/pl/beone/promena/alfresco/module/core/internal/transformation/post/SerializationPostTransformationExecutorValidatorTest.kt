package pl.beone.promena.alfresco.module.core.internal.transformation.post

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowExactly
import io.kotlintest.shouldThrowExactly
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeService
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PostTransformationExecutorValidationException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.transformer.contract.transformation.Transformation

@RunWith(AlfrescoTestRunner::class)
class SerializationPostTransformationExecutorValidatorTest : AbstractAlfrescoIT() {

    companion object {
        private val postTransformationExecutorValidator = SerializationPostTransformationExecutorValidator()
    }

    @Test
    fun validate_object() {
        shouldNotThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(ObjectSerializationPostTransformationExecutorValidatorTest)
        }
    }

    @Test
    fun validate_classWithFields() {
        shouldNotThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(ClassSerializationPostTransformationExecutorValidatorTest("test"))
        }
    }

    @Test
    fun validate_anonymousClass_shouldThrowPostTransformationExecutorValidationException() {
        shouldThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                object : PostTransformationExecutor() {
                    override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
                    }
                }
            )
        }.message shouldBe "Can't be anonymous class"
    }

    @Test
    fun validate_proxyClass_shouldThrowPostTransformationExecutorValidationException() {
        shouldThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                ClassWithProxySerializationPostTransformationExecutorValidatorTest(
                    "test",
                    serviceRegistry.nodeService
                )
            )
        }.message shouldBe "Can't contain beans but has: [nodeService]"
    }

    private object ObjectSerializationPostTransformationExecutorValidatorTest : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }

    private class ClassSerializationPostTransformationExecutorValidatorTest(
        private val test: String
    ) : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }

    private class ClassWithProxySerializationPostTransformationExecutorValidatorTest(
        private val test: String,
        private val nodeService: NodeService
    ) : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }
}