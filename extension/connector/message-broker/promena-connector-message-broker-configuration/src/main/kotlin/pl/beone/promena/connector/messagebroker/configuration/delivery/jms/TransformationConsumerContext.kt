package pl.beone.promena.connector.messagebroker.configuration.delivery.jms

import org.apache.activemq.command.ActiveMQQueue
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.connector.messagebroker.applicationmodel.PromenaJmsHeaders
import pl.beone.promena.connector.messagebroker.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.messagebroker.delivery.jms.TransformationConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

internal typealias TransformerIds = List<String>

@Configuration
class TransformationConsumerContext {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformationConsumerContext::class.java)

        private val transformationIdMessageSelectorDeterminer = TransformationIdMessageSelectorDeterminer()
    }

    @Bean
    fun transformationConsumer(environment: StandardEnvironment,
                               transformers: List<Transformer>,
                               transformerConfig: TransformerConfig,
                               transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
                               jmsTemplate: JmsTemplate,
                               transformationUseCase: TransformationUseCase): TransformationConsumer {
        val messageSelector = transformationIdMessageSelectorDeterminer.determine(transformerConfig, transformers, transformationHashFunctionDeterminer)

        logger.info("Set message selector for TransformationConsumer on <${PromenaJmsHeaders.TRANSFORMATION_ID}>: {}", messageSelector)

        environment.propertySources.addLast(
                MapPropertySource("transformationConsumer",
                                  mapOf("promena.connector.message-broker.consumer.queue.request.message-selector" to messageSelector))
        )

        return TransformationConsumer(jmsTemplate,
                                      ActiveMQQueue(environment.getRequiredProperty("promena.connector.message-broker.consumer.queue.response")),
                                      ActiveMQQueue(environment.getRequiredProperty("promena.connector.message-broker.consumer.queue.response.error")),
                                      transformationUseCase)
    }

}