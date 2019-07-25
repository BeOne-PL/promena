package pl.beone.promena.core.configuration.external.akka.actor

import akka.actor.ActorRef
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.external.akka.actor.GroupedByNameActorService

@Configuration
class GroupedByNameActorServiceContext {

    @Bean
    @ConditionalOnMissingBean(ActorService::class)
    fun groupedByNameActorService(transformerActorsBeanRegister: TransformerActorsBeanRegister, // it forces the creation of transformer actors before injecting transformerActorDescriptors
                                  transformerActorDescriptors: List<TransformerActorDescriptor>,
                                  @Qualifier("serializerActor") serializerActorRef: ActorRef) =
        GroupedByNameActorService(transformerActorDescriptors,
                                  serializerActorRef)
}
