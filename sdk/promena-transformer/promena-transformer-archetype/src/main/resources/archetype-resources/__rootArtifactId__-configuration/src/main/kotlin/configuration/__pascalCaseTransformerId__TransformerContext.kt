package ${package}.configuration

import ${package}.${pascalCaseTransformerId}Transformer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.transformer.contract.communication.CommunicationParameters

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(internalCommunicationParameters: CommunicationParameters) =
        ${pascalCaseTransformerId}Transformer(internalCommunicationParameters)
}