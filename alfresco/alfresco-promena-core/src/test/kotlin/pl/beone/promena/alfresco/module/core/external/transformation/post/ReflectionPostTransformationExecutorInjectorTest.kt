package pl.beone.promena.alfresco.module.core.external.transformation.post

import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.alfresco.model.ContentModel.TYPE_CONTENT
import org.alfresco.service.ServiceRegistry
import org.junit.Test
import org.springframework.context.ApplicationContext
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.transformer.contract.transformation.Transformation

class ReflectionPostTransformationExecutorInjectorTest {

    @Test
    fun inject() {
        val applicationContext = mockk<ApplicationContext> {
            every { id } returns "applicationContext"
        }
        val serviceRegistry = mockk<ServiceRegistry> {
            every { getService(TYPE_CONTENT) } returns "serviceRegistry"
        }

        val postTransformationExecutor = TestPostTransformationExecutor()
        ReflectionPostTransformationExecutorInjector(applicationContext, serviceRegistry).inject(postTransformationExecutor)

        postTransformationExecutor.execute(mockk(), mockk(), mockk())
        postTransformationExecutor.text shouldBe "applicationContext-serviceRegistry"
    }

    private class TestPostTransformationExecutor() : PostTransformationExecutor() {

        lateinit var text: String

        override fun execute(transformation: Transformation, nodeDescriptor: NodeDescriptor, result: TransformationExecutionResult) {
            text = applicationContext.id!! + "-" + serviceRegistry.getService(TYPE_CONTENT)
        }
    }
}