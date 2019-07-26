package pl.beone.promena.core.configuration.external.akka.serialization

import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.external.akka.serialization.AkkaKryoSerializationService

@Configuration
class AkkaKryoSerializationServiceContext {

    @Bean
    @ConditionalOnMissingBean(SerializationService::class)
    fun akkaKryoSerializationService(
        actorMaterializer: ActorMaterializer,
        actorService: ActorService
    ) =
        AkkaKryoSerializationService(
            actorMaterializer,
            actorService
        )
}