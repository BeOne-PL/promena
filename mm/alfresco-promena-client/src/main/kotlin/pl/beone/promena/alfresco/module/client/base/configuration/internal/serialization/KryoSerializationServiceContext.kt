package pl.beone.promena.alfresco.module.client.base.configuration.internal.serialization

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.configuration.getRequiredPropertyWithResolvedPlaceholders
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import java.util.*

@Configuration
class KryoSerializationServiceContext {

    @Bean
    fun kryoSerializationService(@Qualifier("global-properties") properties: Properties) =
        KryoSerializationService(properties.getRequiredPropertyWithResolvedPlaceholders("promena.client.serializer.kryo.buffer-size").toInt())
}