package pl.beone.promena.core.configuration.external.akka.transformer.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.core.external.akka.transformer.config.GroupedByNameTransformersCreator

@Configuration
class GroupedByNameTransformersCreatorContext {

    @Bean
    @ConditionalOnMissingBean(TransformersCreator::class)
    fun groupedByNameTransformersCreator(
        environment: Environment,
        transformerConfig: TransformerConfig,
        internalCommunicationConverter: InternalCommunicationConverter,
        internalCommunicationCleaner: InternalCommunicationCleaner,
        actorCreator: ActorCreator
    ) =
        GroupedByNameTransformersCreator(
            environment.getRequiredProperty("core.transformer.actor.cluster-aware", Boolean::class.java),
            transformerConfig,
            internalCommunicationConverter,
            internalCommunicationCleaner,
            actorCreator
        )
}