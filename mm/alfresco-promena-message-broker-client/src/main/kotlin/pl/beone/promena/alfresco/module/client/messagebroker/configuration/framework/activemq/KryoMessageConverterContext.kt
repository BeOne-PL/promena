package pl.beone.promena.alfresco.module.client.messagebroker.configuration.framework.activemq

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