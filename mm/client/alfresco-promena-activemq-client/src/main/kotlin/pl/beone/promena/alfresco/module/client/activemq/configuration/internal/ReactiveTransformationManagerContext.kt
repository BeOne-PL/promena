package pl.beone.promena.alfresco.module.client.activemq.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.internal.ReactiveTransformationManager

@Configuration
class ReactiveTransformationManagerContext {

    @Bean
    fun reactiveTransformationManager() =
        ReactiveTransformationManager()
}