package pl.beone.promena.core.configuration.internal.communication.external.manager

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.communication.external.manager.ExternalCommunication
import pl.beone.promena.core.internal.communication.external.manager.DefaultExternalCommunicationManager

@Configuration
class DefaultExternalCommunicationManagerContext {

    @Bean
    @ConditionalOnMissingBean
    fun defaultExternalCommunicationManager(environment: Environment,
                                            externalCommunications: List<ExternalCommunication>) =
            DefaultExternalCommunicationManager(externalCommunications,
                                                environment.getRequiredProperty("communication.external.manager.back-pressure.enabled",
                                                                                Boolean::class.java),
                                                environment.getRequiredProperty("communication.external.manager.back-pressure.id"))
}