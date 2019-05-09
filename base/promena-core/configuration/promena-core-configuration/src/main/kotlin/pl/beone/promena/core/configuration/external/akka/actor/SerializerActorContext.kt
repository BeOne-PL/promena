package pl.beone.promena.core.configuration.external.akka.actor

import akka.actor.ActorSystem
import akka.actor.Props
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.actor.serializer.SerializerActor

@Configuration
class SerializerActorContext {

    @Autowired
    private lateinit var actorCreator: ActorCreator

    @Bean
    fun serializerActor(environment: Environment,
                        actorSystem: ActorSystem,
                        serializationService: SerializationService) =
            actorCreator.create("serializer",
                                Props.create(SerializerActor::class.java, serializationService),
                                environment.getRequiredProperty("core.serializer.actors", Int::class.java)).ref

}