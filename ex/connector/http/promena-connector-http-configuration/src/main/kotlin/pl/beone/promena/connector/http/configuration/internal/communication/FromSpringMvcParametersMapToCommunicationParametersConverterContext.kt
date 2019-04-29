package pl.beone.promena.connector.http.configuration.internal.communication

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.connector.http.internal.communication.FromSpringMvcParametersMapToCommunicationParametersConverter

@Configuration
class FromSpringMvcParametersMapToCommunicationParametersConverterContext {

    @Bean
    fun fromSpringMvcParametersMapToCommunicationParametersConverter() =
            FromSpringMvcParametersMapToCommunicationParametersConverter()
}