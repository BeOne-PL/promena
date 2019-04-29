package pl.beone.promena.module.http.client.configuration.internal.serialization

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import java.util.*

@Configuration
class KryoSerializationServiceContext {

    @Bean
    fun kryoSerializationService(@Qualifier("global-properties") properties: Properties) =
            KryoSerializationService(properties.getProperty("promena.core.serializer.kryo.buffer-size").toInt())

}