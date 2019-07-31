package pl.beone.promena.core.configuration.external.akka.actor.serializer

import akka.actor.Props
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.external.akka.actor.serializer.KryoSerializerActor
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoSerializerActorContext {

    companion object {
        private val logger = LoggerFactory.getLogger(ActorCreator::class.java)

        private val basedOnTransformersNumberOfSerializerActorsDeterminer = BasedOnTransformersNumberOfSerializerActorsDeterminer()
    }

    @Bean
    @ConditionalOnMissingBean(name = ["serializerActor"])
    fun serializerActor(
        environment: Environment,
        actorCreator: ActorCreator,
        transformerActorDescriptors: List<TransformerActorDescriptor>,
        kryoSerializationService: KryoSerializationService
    ) =
        actorCreator.create(
            KryoSerializerActor.actorName,
            Props.create(KryoSerializerActor::class.java) {
                KryoSerializerActor(kryoSerializationService)
            },
            environment.getProperty("core.serializer.actors", Int::class.java) ?:
                    basedOnTransformersNumberOfSerializerActorsDeterminer.determine(transformerActorDescriptors)
                        .also { logger.info("Property <core.serializer.actors> isn't set. Created actors (the sum of the transformer actors): {}", it) }
        )
}