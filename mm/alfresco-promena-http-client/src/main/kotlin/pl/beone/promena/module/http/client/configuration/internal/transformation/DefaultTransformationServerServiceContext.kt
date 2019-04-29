package pl.beone.promena.module.http.client.configuration.internal.transformation

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.module.http.client.configuration.external.alfresco.getAndVerifyLocation
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.lib.http.client.contract.request.TransformationExecutor
import pl.beone.promena.lib.http.client.internal.transformation.DefaultTransformationServerService
import java.util.*

@Configuration
class DefaultTransformationServerServiceContext {

    @Bean
    fun defaultTransformationServerService(@Qualifier("global-properties") properties: Properties,
                                           requestExecutor: TransformationExecutor,
                                           serializationService: SerializationService) =
            DefaultTransformationServerService(requestExecutor,
                                               serializationService,
                                               properties.getProperty("promena.transformer.timeout").toLong(),
                                               properties.getAndVerifyLocation())
}