package pl.beone.promena.core.configuration.external.spring.transformer.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.external.spring.transformer.config.PropertiesTransformerConfig

@Configuration
class PropertiesTransformerConfigContext {

    @Bean
    @ConditionalOnMissingBean(TransformerConfig::class)
    fun propertiesTransformerConfig(environment: Environment) =
            PropertiesTransformerConfig(environment)
}