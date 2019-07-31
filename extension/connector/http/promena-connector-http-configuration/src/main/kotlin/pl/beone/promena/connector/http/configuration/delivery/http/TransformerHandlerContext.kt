package pl.beone.promena.connector.http.configuration.delivery.http

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.http.delivery.http.TransformerHandler
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.contract.transformation.TransformationUseCase

@Configuration
class TransformerHandlerContext {

    @Bean
    fun transformerHandler(
        @Qualifier("akkaKryoSerializationService") serializationService: SerializationService,
        transformationUseCase: TransformationUseCase
    ) =
        TransformerHandler(serializationService, transformationUseCase)
}