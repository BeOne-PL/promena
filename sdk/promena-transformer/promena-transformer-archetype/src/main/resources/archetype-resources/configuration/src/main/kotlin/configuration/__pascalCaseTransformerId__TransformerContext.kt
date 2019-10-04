package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(
        settings: ${pascalCaseTransformerId}TransformerSettings,
        defaultParameters: ${pascalCaseTransformerId}TransformerDefaultParameters,
        internalCommunicationParameters: CommunicationParameters
    ) =
        ${pascalCaseTransformerId}Transformer(
            settings,
            defaultParameters,
            internalCommunicationParameters
        )
}