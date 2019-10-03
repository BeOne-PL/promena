package pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.alfresco.module.connector.activemq.GlobalPropertiesContext
import pl.beone.promena.alfresco.module.connector.activemq.applicationmodel.TransformationParameters
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.ActiveMQContainerContext
import pl.beone.promena.alfresco.module.connector.activemq.delivery.activemq.context.SetupContext
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager
import pl.beone.promena.alfresco.module.core.applicationmodel.retry.customRetry
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import pl.beone.promena.transformer.internal.model.parameters.plus
import java.time.Duration
import java.util.*

@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:alfresco-global-test.properties"])
@ContextHierarchy(
    ContextConfiguration(classes = [ActiveMQContainerContext::class, GlobalPropertiesContext::class]),
    ContextConfiguration(classes = [SetupContext::class])
)
class TransformerResponseErrorAttemptsExceededFlowTest {

    @Autowired
    private lateinit var jmsUtils: JmsUtils

    @Autowired
    private lateinit var reactiveTransformationManager: ReactiveTransformationManager

    companion object {
        private val id = UUID.randomUUID().toString()
        private const val attempt = 2L
        private val transformationParameters = TransformationParameters(
            emptyList(),
            "123456789",
            customRetry(0, Duration.ZERO),
            attempt,
            "admin"
        )
        private val exception = TransformationException(
            singleTransformation("transformer-test", APPLICATION_PDF, emptyParameters() + ("key" to "value")),
            "Exception"
        )
    }

    @After
    fun tearDown() {
        jmsUtils.dequeueQueues()
    }

    @Test
    fun `shouldn't receive any message because the number of attempts has been exceeded`() {
        val transformation = reactiveTransformationManager.startTransformation(id)

        jmsUtils.sendResponseErrorMessage(
            id,
            exception,
            transformationParameters
        )

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofSeconds(1))
        }.message shouldContain "Timeout on blocking read for"

        jmsUtils.getTransformationParametersFromResponseError().attempt shouldBe attempt
    }
}