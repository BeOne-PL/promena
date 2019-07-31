package pl.beone.promena.core.configuration.internal.serialization

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoSerializationServiceContext {

    @Bean
    fun kryoSerializationService(environment: Environment) =
        KryoSerializationService(environment.getRequiredProperty("core.serializer.kryo.buffer-size", Int::class.java))
}