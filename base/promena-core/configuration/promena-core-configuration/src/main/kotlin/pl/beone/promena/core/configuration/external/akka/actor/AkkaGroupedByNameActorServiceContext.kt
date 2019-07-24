package pl.beone.promena.core.configuration.external.akka.actor

import akka.actor.ActorRef
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.external.akka.actor.AkkaGroupedByNameActorService

@Configuration
class AkkaGroupedByNameActorServiceContext {

    @Bean
    @ConditionalOnMissingBean(ActorService::class)
    fun akkaGroupedByNameActorService(transformerActorsBeanRegister: TransformerActorsBeanRegister, // it forces the creation of transformer actors before injecting actorRefWithTransformerIdList
                                      actorRefWithTransformerIdList: List<ActorRefWithTransformerId>,
                                      @Qualifier("serializerActor") serializerActorRef: ActorRef) =
        AkkaGroupedByNameActorService(actorRefWithTransformerIdList,
                                      serializerActorRef)
}
