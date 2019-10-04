package ${package}.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

@Configuration
class ${pascalCaseTransformerId}TransformerContext {

    @Bean
    fun ${camelCaseTransformerId}Transformer(
        environment: Environment,
        internalCommunicationParameters: CommunicationParameters
    ) =
        ${pascalCaseTransformerId}Transformer(
            createSettings(environment),
            createDefaultParameters(environment),
            internalCommunicationParameters
        )

    private fun createSettings(environment: Environment): ${pascalCaseTransformerId}TransformerSettings =
        ${pascalCaseTransformerId}TransformerSettings(
            environment.getRequiredProperty("transformer.${package}.settings.example")
        )

    private fun createDefaultParameters(environment: Environment): ${pascalCaseTransformerId}TransformerDefaultParameters =
        ${pascalCaseTransformerId}TransformerDefaultParameters(
            environment.getRequiredProperty("transformer.${package}.default.parameters.example2")
        )
}