package pl.beone.promena.core.configuration.internal.serialization

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoSerializationServiceContext {

    @Bean
    fun kryoSerializationService() =
            KryoSerializationService()
}