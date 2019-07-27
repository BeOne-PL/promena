package pl.beone.promena.core.configuration.external.akka.transformer.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.core.external.akka.transformer.config.GroupedByNameTransformersCreator

@Configuration
class GroupedByNameTransformersCreatorContext {

    @Bean
    @ConditionalOnMissingBean(TransformersCreator::class)
    fun groupedByNameTransformersCreator(
        transformerConfig: TransformerConfig,
        internalCommunicationConverter: InternalCommunicationConverter,
        actorCreator: ActorCreator
    ) =
        GroupedByNameTransformersCreator(
            transformerConfig,
            internalCommunicationConverter,
            actorCreator
        )
}