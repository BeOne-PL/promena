package pl.beone.promena.alfresco.module.connector.activemq.configuration.external.springmessaging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter
import pl.beone.promena.core.internal.serialization.KryoSerializationService

@Configuration
class KryoMessageConverterContext {

    @Bean
    fun kryoMessageConverter(
        kryoSerializationService: KryoSerializationService
    ) =
        KryoMessageConverter(kryoSerializationService)
}