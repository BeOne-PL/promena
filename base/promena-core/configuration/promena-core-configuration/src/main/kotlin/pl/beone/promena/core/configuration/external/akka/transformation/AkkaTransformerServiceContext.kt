package pl.beone.promena.core.configuration.external.akka.transformation

import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.transformation.AkkaTransformationService

@Configuration
class AkkaTransformerServiceContext {

    @Bean
    @ConditionalOnMissingBean(TransformationService::class)
    fun akkaTransformationService(
        actorMaterializer: ActorMaterializer,
        actorService: ActorService
    ) =
        AkkaTransformationService(
            actorMaterializer,
            actorService
        )
}