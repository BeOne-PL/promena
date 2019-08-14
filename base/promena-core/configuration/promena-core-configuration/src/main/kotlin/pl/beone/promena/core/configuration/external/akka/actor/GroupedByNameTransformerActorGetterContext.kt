package pl.beone.promena.core.configuration.external.akka.actor

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.TransformerActorGetter
import pl.beone.promena.core.external.akka.actor.GroupedByNameTransformerActorGetter

@Configuration
class GroupedByNameTransformerActorGetterContext {

    @Bean
    @DependsOn("transformerActorsBeanRegister")
    @ConditionalOnMissingBean(TransformerActorGetter::class)
    fun groupedByNameTransformerActorGetter(
        transformerActorDescriptors: List<TransformerActorDescriptor>
    ) =
        GroupedByNameTransformerActorGetter(
            transformerActorDescriptors
        )
}
