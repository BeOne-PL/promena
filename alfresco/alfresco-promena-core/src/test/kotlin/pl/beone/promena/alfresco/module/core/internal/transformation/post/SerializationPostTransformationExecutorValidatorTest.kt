package pl.beone.promena.alfresco.module.core.internal.transformation.post

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowExactly
import io.kotlintest.shouldThrowExactly
import mu.KotlinLogging
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeService
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PostTransformationExecutorValidationException
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import pl.beone.promena.transformer.contract.transformation.Transformation

@RunWith(AlfrescoTestRunner::class)
class SerializationPostTransformationExecutorValidatorTest : AbstractAlfrescoIT() {

    companion object {
        private val postTransformationExecutorValidator = SerializationPostTransformationExecutorValidator(KryoSerializationService())
    }

    @Test
    fun validate_object() {
        shouldNotThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(ObjectPostTransformationExecutorValidatorTest)
        }
    }

    @Test
    fun validate_withFieldAndLogger() {
        shouldNotThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                FieldAndLoggerPostTransformationExecutorValidatorTest("test")
            )
        }

        shouldNotThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                JavaSerializationPostTransformationExecutorValidatorTest.FieldAndLoggerPostTransformationExecutorValidatorTest("test")
            )
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
        }.message shouldBe "PostTransformationExecutor implementation can't be anonymous class"
    }

    @Test
    fun validate_beanClass_shouldThrowPostTransformationExecutorValidationException() {
        shouldThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                BeanPostTransformationExecutorValidatorTest("test", serviceRegistry.nodeService)
            )
        }.message shouldBe "PostTransformationExecutor can't contain fields with reference to ClassLoader (for example: proxy class, object with BeanFactory field etc.) but has: [nodeService]"
    }

    @Test
    fun validate_nonStaticLogger_shouldThrowPostTransformationExecutorValidationException() {
        shouldThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                NonStaticLoggerPostTransformationExecutorValidatorTest()
            )
        }.message shouldBe "PostTransformationExecutor can't contain non-static logger fields but has: [logger]"

        shouldThrowExactly<PostTransformationExecutorValidationException> {
            postTransformationExecutorValidator.validate(
                JavaSerializationPostTransformationExecutorValidatorTest.NonStaticLoggerPostTransformationExecutorValidatorTest()
            )
        }.message shouldBe "PostTransformationExecutor can't contain non-static logger fields but has: [logger]"
    }

    private object ObjectPostTransformationExecutorValidatorTest : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }

    private class FieldAndLoggerPostTransformationExecutorValidatorTest(
        private val test: String
    ) : PostTransformationExecutor() {

        companion object {
            private val logger = KotlinLogging.logger {}
        }

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }

    private class BeanPostTransformationExecutorValidatorTest(
        private val test: String,
        private val nodeService: NodeService
    ) : PostTransformationExecutor() {

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }

    private class NonStaticLoggerPostTransformationExecutorValidatorTest : PostTransformationExecutor() {

        private val logger = KotlinLogging.logger {}

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
        }
    }
}