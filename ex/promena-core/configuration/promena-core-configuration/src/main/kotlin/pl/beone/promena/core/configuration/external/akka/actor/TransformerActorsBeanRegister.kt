package pl.beone.promena.core.configuration.external.akka.actor

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.transformer.contract.Transformer
import javax.annotation.PostConstruct

@Configuration
class TransformerActorsBeanRegister {

    @Autowired
    private lateinit var beanFactory: ConfigurableBeanFactory

    @Autowired(required = false)
    private lateinit var transformers: List<Transformer>

    @Autowired
    private lateinit var transformerActorsCreator: TransformersCreator

    @PostConstruct
    fun register() {
        transformerActorsCreator.create(if (!::transformers.isInitialized) emptyList() else transformers)
                .forEach { beanFactory.registerSingleton(it.id, it) }
    }


}