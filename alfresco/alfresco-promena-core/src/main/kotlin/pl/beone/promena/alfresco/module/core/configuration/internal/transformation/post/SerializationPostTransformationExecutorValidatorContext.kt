package pl.beone.promena.alfresco.module.core.configuration.internal.transformation.post

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.internal.transformation.post.SerializationPostTransformationExecutorValidator

@Configuration
class SerializationPostTransformationExecutorValidatorContext {

    @Bean
    fun serializationPostTransformationExecutorValidator() =
        SerializationPostTransformationExecutorValidator()
}