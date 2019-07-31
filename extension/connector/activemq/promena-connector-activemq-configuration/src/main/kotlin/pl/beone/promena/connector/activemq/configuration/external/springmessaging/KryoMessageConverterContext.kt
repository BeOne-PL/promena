package pl.beone.promena.connector.activemq.configuration.external.springmessaging

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.activemq.delivery.jms.message.converter.KryoMessageConverter
import pl.beone.promena.core.contract.serialization.SerializationService

@Configuration
class KryoMessageConverterContext {

    @Bean
    fun kryoMessageConverter(
        @Qualifier("akkaKryoSerializationService") serializationService: SerializationService
    ) =
        KryoMessageConverter(serializationService)
}