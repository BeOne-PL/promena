package pl.beone.promena.connector.activemq.integrationtest.dependency

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoSerializationServiceContext {

    @Bean
    fun kryoSerializationService() =
        KryoSerializationService()
}