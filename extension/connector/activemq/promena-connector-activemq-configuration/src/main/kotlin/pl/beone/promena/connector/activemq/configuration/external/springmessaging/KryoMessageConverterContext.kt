package pl.beone.promena.connector.activemq.configuration.external.springmessaging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.lib.jms.message.converter.KryoMessageConverter
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoMessageConverterContext {

    @Bean
    fun kryoMessageConverter(kryoSerializationService: KryoSerializationService) =
            KryoMessageConverter(kryoSerializationService)
}