package pl.beone.promena.core.configuration.external.akka.transformer

import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.core.external.akka.transformer.AkkaTransformerService

@Configuration
class AkkaTransformerServiceContext {

    @Bean
    @ConditionalOnMissingBean(TransformerService::class)
    fun akkaTransformerService(actorMaterializer: ActorMaterializer,
                               actorService: ActorService) =
            AkkaTransformerService(actorMaterializer,
                                   actorService)
}