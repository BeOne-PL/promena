package pl.beone.promena.connector.activemq.configuration.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.connector.activemq.delivery.jms.TransformerConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.config.TransformerConfig

@Configuration
class TransformerConsumerContext {

    @Bean
    fun transformerConsumer(environment: StandardEnvironment,
                            transformerConfigs: List<TransformerConfig>,
                            jmsTemplate: JmsTemplate,
                            transformationUseCase: TransformationUseCase): TransformerConsumer {
        addSelectorToEnvironment(environment, transformerConfigs)

        return TransformerConsumer(jmsTemplate,
                ActiveMQQueue(environment.getRequiredProperty("activemq.promena.consumer.queue.response")),
                ActiveMQQueue(environment.getRequiredProperty("activemq.promena.consumer.queue.response.error")),
                transformationUseCase)
    }

    private fun addSelectorToEnvironment(environment: StandardEnvironment, transformerConfigs: List<TransformerConfig>) {
        val selector = transformerConfigs.joinToString(" OR ") { "transformerId = '$it'" }

        environment.propertySources.addLast(
                MapPropertySource("transformerConsumer",
                        mapOf("activemq.promena.consumer.selector" to selector))
        )
    }
}