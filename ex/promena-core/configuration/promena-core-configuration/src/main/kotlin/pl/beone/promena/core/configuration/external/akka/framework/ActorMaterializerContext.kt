package pl.beone.promena.core.configuration.external.akka.framework

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ActorMaterializerContext {

    @Bean
    fun actorMaterializer(actorSystem: ActorSystem): ActorMaterializer =
            ActorMaterializer.create(actorSystem)
}