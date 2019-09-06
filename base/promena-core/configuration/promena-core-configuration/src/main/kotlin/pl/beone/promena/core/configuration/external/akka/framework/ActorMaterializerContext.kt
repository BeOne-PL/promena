package pl.beone.promena.core.configuration.external.akka.framework

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ActorMaterializerContext {

    @Bean
    @ConditionalOnMissingBean(ActorMaterializer::class)
    fun actorMaterializer(
        actorSystem: ActorSystem
    ): ActorMaterializer =
        ActorMaterializer.create(
            actorSystem
        )
}