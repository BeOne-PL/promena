package pl.beone.promena.core.configuration.external.akka.serialization

import akka.stream.ActorMaterializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.serialization.DescriptorSerializationService
import pl.beone.promena.core.external.akka.serialization.AkkaDescriptorSerializationService

@Configuration
class AkkaDescriptorSerializationServiceContext {

    @Bean
    @ConditionalOnMissingBean(DescriptorSerializationService::class)
    fun akkaDescriptorSerializationService(actorMaterializer: ActorMaterializer,
                                           actorService: ActorService) =
            AkkaDescriptorSerializationService(actorMaterializer,
                                               actorService)
}