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
import pl.beone.promena.core.external.akka.serialization.AkkaSerializationService
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class AkkaSerializationServiceContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    @ConditionalOnMissingBean(SerializationService::class)
    fun akkaSerializationService(
        environment: Environment,
        actorMaterializer: ActorMaterializer,
        actorCreator: ActorCreator,
        transformerActorDescriptors: List<TransformerActorDescriptor>
    ) =
        AkkaSerializationService(
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
            getNumberOfActors(environment) ?: determineNumberOfActors(transformerActorDescriptors),
            environment.getRequiredProperty("core.serializer.actor.cluster-aware", Boolean::class.java)
        )

    private fun createSerializationService(environment: Environment): KryoSerializationService =
        KryoSerializationService(environment.getRequiredProperty("core.serializer.kryo.buffer-size", Int::class.java))

    private fun getNumberOfActors(environment: Environment): Int? =
        environment.getNotBlankProperty("core.serializer.actors")?.toInt()
            ?.also { logger.info { "Created serializer actors: <$it>" } }

    private fun determineNumberOfActors(transformerActorDescriptors: List<TransformerActorDescriptor>): Int =
        determine(transformerActorDescriptors)
            .also { logger.info { "Property <core.serializer.actors> isn't set. Created serializer actors (sum of transformer actors): <$it>" } }

    private fun Environment.getNotBlankProperty(key: String): String? {
        val value = getRequiredProperty(key)
        return if (value.isNotBlank()) {
            value
        } else {
            null
        }
    }
}