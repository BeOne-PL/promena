package pl.beone.promena.connector.messagebroker.configuration.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.connector.messagebroker.delivery.jms.PromenaJmsHeader.PROMENA_TRANSFORMER_ID
import pl.beone.promena.connector.messagebroker.delivery.jms.TransformerConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

@Configuration
class TransformerConsumerContext {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerConsumerContext::class.java)
    }

    @Bean
    fun transformerConsumer(environment: StandardEnvironment,
                            transformers: List<Transformer>,
                            transformerConfig: TransformerConfig,
                            jmsTemplate: JmsTemplate,
                            transformationUseCase: TransformationUseCase): TransformerConsumer {
        addSelectorToEnvironment(environment, transformerConfig, transformers)

        return TransformerConsumer(jmsTemplate,
                                   ActiveMQQueue(environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response")),
                                   ActiveMQQueue(environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response.error")),
                                   transformationUseCase)
    }

    private fun addSelectorToEnvironment(environment: StandardEnvironment,
                                         transformerConfig: TransformerConfig,
                                         transformers: List<Transformer>) {
        val selector =
                transformers.joinToString(" OR ") { "$PROMENA_TRANSFORMER_ID = '${transformerConfig.getTransformationId(it)}'" }

        logger.info("Set transformer consumer selector to: {}", selector)

        environment.propertySources.addLast(
                MapPropertySource("transformerConsumer", mapOf("promena.connector.activemq.consumer.queue.request.selector" to selector))
        )
    }
}