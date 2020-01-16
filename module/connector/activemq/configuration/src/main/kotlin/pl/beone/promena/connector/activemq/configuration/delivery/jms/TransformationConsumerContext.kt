package pl.beone.promena.connector.activemq.configuration.delivery.jms

import mu.KotlinLogging
import org.apache.activemq.command.ActiveMQQueue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.jms.core.JmsTemplate
import pl.beone.promena.connector.activemq.applicationmodel.PromenaJmsHeaders.TRANSFORMATION_HASH_CODE
import pl.beone.promena.connector.activemq.contract.TransformationHashFunctionDeterminer
import pl.beone.promena.connector.activemq.delivery.jms.TransformationConsumer
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.transformer.contract.Transformer

@Configuration
class TransformationConsumerContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    fun transformationConsumer(
        environment: StandardEnvironment,
        transformers: List<Transformer>,
        transformerConfig: TransformerConfig,
        transformationHashFunctionDeterminer: TransformationHashFunctionDeterminer,
        jmsTemplate: JmsTemplate,
        transformationUseCase: TransformationUseCase
    ): TransformationConsumer {
        val messageSelector =
            TransformationHashFunctionMessageSelectorDeterminer.determine(transformerConfig, transformers, transformationHashFunctionDeterminer)

        logger.debug { "Set message selector for TransformationConsumer on <$TRANSFORMATION_HASH_CODE>: <$messageSelector>" }

        environment.propertySources.addLast(
            MapPropertySource(
                "transformationConsumer",
                mapOf("promena.connector.activemq.consumer.queue.request.message-selector" to messageSelector)
            )
        )

        return TransformationConsumer(
            jmsTemplate,
            ActiveMQQueue(environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response")),
            ActiveMQQueue(environment.getRequiredProperty("promena.connector.activemq.consumer.queue.response.error")),
            transformationUseCase
        )
    }

}