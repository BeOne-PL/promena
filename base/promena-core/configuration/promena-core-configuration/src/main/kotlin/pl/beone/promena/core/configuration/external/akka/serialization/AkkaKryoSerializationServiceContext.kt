package pl.beone.promena.core.configuration.external.akka.serialization

import akka.actor.ActorRef
import akka.actor.Props
import akka.stream.ActorMaterializer
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.KryoSerializerActor
import pl.beone.promena.core.external.akka.serialization.AkkaKryoSerializationService
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class AkkaKryoSerializationServiceContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    @ConditionalOnMissingBean(SerializationService::class)
    fun akkaKryoSerializationService(
        actorMaterializer: ActorMaterializer,
        environment: Environment,
        actorCreator: ActorCreator,
        transformerActorDescriptors: List<TransformerActorDescriptor>
    ) =
        AkkaKryoSerializationService(
            actorMaterializer,
            createSerializerActor(environment, actorCreator, transformerActorDescriptors)
        )

    private fun createSerializerActor(
        environment: Environment,
        actorCreator: ActorCreator,
        transformerActorDescriptors: List<TransformerActorDescriptor>
    ): ActorRef =
        actorCreator.create(
            KryoSerializerActor.actorName,
            Props.create(KryoSerializerActor::class.java) { KryoSerializerActor(createSerializationService(environment)) },
            environment.getProperty("core.serializer.actors", Int::class.java)?.also { logger.info { "Created serializer actors: $it" } }
                ?: determine(transformerActorDescriptors).also { logger.info { "Property <core.serializer.actors> isn't set. Created serializer actors (the sum of the transformer actors): $it" } }
        )

    private fun createSerializationService(environment: Environment): KryoSerializationService =
        KryoSerializationService(environment.getRequiredProperty("core.serializer.kryo.buffer-size", Int::class.java))
}