package pl.beone.promena.alfresco.module.core.configuration.external.transformation.post

import org.alfresco.service.ServiceRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.transformation.post.ReflectionPostTransformationExecutorInjector

@Configuration
class ReflectionPostTransformationExecutorInjectorContext {

    @Bean
    fun reflectionPostTransformationExecutorInjector(
        applicationContext: ApplicationContext,
        serviceRegistry: ServiceRegistry
    ) =
        ReflectionPostTransformationExecutorInjector(
            applicationContext,
            serviceRegistry
        )
}