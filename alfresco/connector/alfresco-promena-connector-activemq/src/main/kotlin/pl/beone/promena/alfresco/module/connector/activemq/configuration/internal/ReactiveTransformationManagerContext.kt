package pl.beone.promena.alfresco.module.connector.activemq.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.connector.activemq.internal.ReactiveTransformationManager

@Configuration
class ReactiveTransformationManagerContext {

    @Bean
    fun reactiveTransformationManager() =
        ReactiveTransformationManager()
}