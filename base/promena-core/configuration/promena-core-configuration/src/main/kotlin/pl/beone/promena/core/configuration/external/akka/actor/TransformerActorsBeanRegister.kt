package pl.beone.promena.core.configuration.external.akka.actor

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.transformer.contract.Transformer
import java.util.*
import javax.annotation.PostConstruct

@Configuration
class TransformerActorsBeanRegister(private val beanFactory: ConfigurableBeanFactory,
                                    private val transformers: Optional<List<Transformer>>,
                                    private val transformerActorsCreator: TransformersCreator) {

    @PostConstruct
    fun register() {
        transformerActorsCreator.create(transformers.orElse(emptyList()))
                .forEach { beanFactory.registerSingleton(it.id, it) }
    }


}