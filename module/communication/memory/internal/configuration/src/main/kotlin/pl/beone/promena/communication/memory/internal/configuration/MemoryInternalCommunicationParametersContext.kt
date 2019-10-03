package pl.beone.promena.communication.memory.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.communication.memory.model.internal.memoryCommunicationParameters

@Configuration
class MemoryInternalCommunicationParametersContext {

    @Bean
    fun internalCommunicationParameters(
        environment: Environment
    ) =
        memoryCommunicationParameters()
}