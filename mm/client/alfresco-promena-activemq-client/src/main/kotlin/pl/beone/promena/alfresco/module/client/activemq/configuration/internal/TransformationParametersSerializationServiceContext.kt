package pl.beone.promena.alfresco.module.client.activemq.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.activemq.internal.TransformationParametersSerializationService
import pl.beone.promena.core.contract.serialization.SerializationService

@Configuration
class TransformationParametersSerializationServiceContext {

    @Bean
    fun transformationParametersSerializationService(
        serializationService: SerializationService
    ) =
        TransformationParametersSerializationService(
            serializationService
        )
}