package pl.beone.promena.core.configuration.external.akka.actor

import akka.actor.ActorSystem
import akka.actor.Props
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.external.akka.actor.serializer.KryoSerializerActor
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoSerializerActorContext(
    private val actorCreator: ActorCreator
) {

    @Bean
    @ConditionalOnBean(name = ["serializerActor"])
    fun serializerActor(
        environment: Environment,
        actorSystem: ActorSystem
    ) =
        actorCreator.create(
            KryoSerializerActor.actorName,
            Props.create(KryoSerializerActor::class.java) {
                KryoSerializerActor(KryoSerializationService(environment.getRequiredProperty("core.serializer.kryo.buffer-size", Int::class.java)))
            },
            environment.getRequiredProperty("core.serializer.actors", Int::class.java)
        )

}