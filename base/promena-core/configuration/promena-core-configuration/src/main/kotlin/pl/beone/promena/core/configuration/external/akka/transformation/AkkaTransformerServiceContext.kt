package pl.beone.promena.core.configuration.external.akka.transformation

import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.configuration.toDuration
import pl.beone.promena.core.contract.actor.ActorGetter
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.transformation.AkkaTransformationService

@Configuration
class AkkaTransformerServiceContext {

    @Bean
    @ConditionalOnMissingBean(TransformationService::class)
    fun akkaTransformationService(
        environment: Environment,
        actorMaterializer: ActorMaterializer,
        actorGetter: ActorGetter
    ) =
        AkkaTransformationService(
            environment.getRequiredProperty("core.transformation.interruption-timeout-delay").toDuration(),
            actorMaterializer,
            actorGetter
        )
}