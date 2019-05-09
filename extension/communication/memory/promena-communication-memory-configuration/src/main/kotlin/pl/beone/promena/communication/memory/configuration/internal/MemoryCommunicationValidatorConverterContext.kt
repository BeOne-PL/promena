package pl.beone.promena.communication.memory.configuration.internal

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.communication.memory.internal.MemoryCommunicationValidatorConverter

@Configuration
class MemoryCommunicationValidatorConverterContext {

    @Bean
    fun memoryCommunicationValidatorConverter() =
            MemoryCommunicationValidatorConverter()
}