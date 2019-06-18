package pl.beone.promena.alfresco.module.client.messagebroker.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.messagebroker.internal.ReactiveTransformationManager

@Configuration
class ReactiveTransformationManagerContext {

    @Bean
    fun reactiveTransformationManager() =
            ReactiveTransformationManager()
}