package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import ${package}.${pascalCaseTransformerId}Transformer

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(internalCommunicationParameters: CommunicationParameters) =
        ${pascalCaseTransformerId}Transformer(internalCommunicationParameters)
}